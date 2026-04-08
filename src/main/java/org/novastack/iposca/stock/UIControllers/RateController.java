package org.novastack.iposca.stock.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.stock.Stock;
import org.novastack.iposca.stock.StockEnums;
import org.novastack.iposca.config.AppConfig;
import org.novastack.iposca.utils.ui.CommonCalls;
import org.novastack.iposca.utils.ui.IValid;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class RateController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        refreshTable();
        rateSpinner.setValueFactory(rateValueFactory);

        FilteredList<Stock> filteredData = new FilteredList<>(markuprates, c -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(stock -> {
                if (newVal == null || newVal.isEmpty()) {return true;}
                String filter = newVal.toLowerCase();
                return stock.getName().toLowerCase().contains(filter);
            });
        });

        SortedList<Stock> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(stockTable.comparatorProperty());
        stockTable.setItems(sortedData);
    }

    private ObservableList<Stock> markuprates = FXCollections.observableArrayList();
    SpinnerValueFactory<Integer> rateValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 1);

    @FXML
    private TableColumn<Stock,Integer> stockID;

    @FXML
    private Button editingRate;

    @FXML
    private TableColumn<Stock, String> name;

    @FXML
    private TextField newMarkupRate;

    @FXML
    private TableColumn<Stock, Integer> markupRate;

    @FXML
    private Spinner<Integer> rateSpinner;

    @FXML
    private TextField searchField;

    @FXML
    private Label success;

    @FXML
    private TableView<Stock> stockTable;

    @FXML
    void changeAllVAT(MouseEvent event) throws IOException {
        success.setVisible(false);
        if (rateSpinner.getValue() == null || !IValid.checkRate(rateSpinner.getValue().toString())) {
            new CommonCalls().openErrorDialog("The value is either invalid or empty!");
            return;
        }
        int newRate = rateSpinner.getValue();
    //    AppConfig appConfig = new AppConfig(org.novastack.iposca.config.AppConfig.ConfigKey.VAT, String.valueOf(newRate));
      //  appConfig.configure(appConfig);
     //   success.setVisible(true);
    }

    @FXML
    void changeMarkupRate(MouseEvent event) throws IOException {
        if (stockTable.getSelectionModel().getSelectedItem() == null ) {
            new CommonCalls().openErrorDialog("Please select an item!");
            return;
        }
        int currentMarkupRate = stockTable.getSelectionModel().getSelectedItem().getMarkupRate();
        if (!IValid.checkRate(newMarkupRate.getText()) || newMarkupRate.getText().isEmpty()) {
            new CommonCalls().openErrorDialog("Please enter a valid markup rate!");
            return;
        }
        int newRate = Integer.parseInt(newMarkupRate.getText());
        if (currentMarkupRate == newRate) {
            return;
        }
        boolean ok = new CommonCalls().openConfirmationDialog("Are you sure you want to change markup rate!");
        if (!ok) {
            return;
        }
        Stock.changeMarkupRate(stockTable.getSelectionModel().getSelectedItem().getId(), newRate);
        refreshTable();
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) stockTable.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/stock/StockManagement.fxml", "Stock Management");
    }

    private void refreshTable() {
        stockID.setCellValueFactory(new PropertyValueFactory<Stock, Integer>("item_id"));
        name.setCellValueFactory(new PropertyValueFactory<Stock, String>("name"));
        markupRate.setCellValueFactory(new PropertyValueFactory<Stock, Integer>("markupRate"));

        try {
            ArrayList<Stock> list = Stock.getAllMarkupRates();
            markuprates.setAll(list);
            stockTable.setItems(markuprates);
        } catch (DataAccessException e) {
            try {
                new CommonCalls().openErrorDialog(e.getMessage());
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            Stage stage = (Stage) stockTable.getScene().getWindow();
            try {
                new CommonCalls().traverse(stage, "/ui/stock/StockManagement.fxml", "Stock Management");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


}
