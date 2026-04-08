package org.novastack.iposca.stock.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.stock.Stock;
import org.novastack.iposca.stock.StockEnums;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ManagementController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        refreshTable();

        FilteredList<Stock> filteredData = new FilteredList<>(stock, c -> true);
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

    ObservableList<Stock> stock = FXCollections.observableArrayList();

    private void refreshTable() {
        id.setCellValueFactory(new PropertyValueFactory<Stock, Integer>("item_id"));
        name.setCellValueFactory(new PropertyValueFactory<Stock, String>("name"));
        productType.setCellValueFactory(new PropertyValueFactory<Stock, String>("productType"));
        packageType.setCellValueFactory(new PropertyValueFactory<Stock, String>("packageType"));
        units.setCellValueFactory(new PropertyValueFactory<Stock, String>("units"));
        unitsInAPack.setCellValueFactory(new PropertyValueFactory<Stock, Integer>("unitsInAPack"));
        bulkCost.setCellValueFactory(new PropertyValueFactory<Stock, Float>("bulkCost"));
        markupRate.setCellValueFactory(new PropertyValueFactory<Stock, Integer>("markupRate"));
        quantity.setCellValueFactory(new PropertyValueFactory<Stock, Integer>("quantity"));
        stockLimit.setCellValueFactory(new PropertyValueFactory<Stock, Integer>("stockLimit"));


        try {
            ArrayList<Stock> list = Stock.getAllStock();
            stock.setAll(list);
            stockTable.setItems(stock);
        } catch (DataAccessException e) {
            try {
                new CommonCalls().openErrorDialog(e.getMessage());
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            Stage stage = (Stage) backButton.getScene().getWindow();
            try {
                new CommonCalls().traverse(stage, "/ui/stock/StockMenu.fxml", "Stock");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @FXML
    private Button addButton;

    @FXML
    private Button backButton;

    @FXML
    private TableColumn<Stock, Float> bulkCost;

    @FXML
    private TableColumn<Stock, Integer> id;

    @FXML
    private TableColumn<Stock, Integer> markupRate;

    @FXML
    private TableView<Stock> stockTable;

    @FXML
    private Button deleteButton;

    @FXML
    private Button editButton;

    @FXML
    private TableColumn<Stock, String> name;

    @FXML
    private TableColumn<Stock, String> packageType;

    @FXML
    private TableColumn<Stock, String> productType;

    @FXML
    private TableColumn<Stock, Integer> quantity;

    @FXML
    private TextField searchField;

    @FXML
    private TableColumn<Stock, Integer> stockLimit;

    @FXML
    private TableColumn<Stock, String> units;

    @FXML
    private TableColumn<Stock, Integer> unitsInAPack;

    @FXML
    void addPage(MouseEvent event) throws IOException {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/stock/AddingNonIPOSStock.fxml", "Stock");
    }

    @FXML
    void deleteStock(MouseEvent event) throws IOException {
        if (stockTable.getSelectionModel().getSelectedItem() == null) {
            new CommonCalls().openErrorDialog("Please select a stock item!");
            return;
        }

        boolean ok = new CommonCalls().openConfirmationDialog("Are you sure you want to delete this stock item?");
        if (!ok) {
            return;
        }

        try {
            Stock.deleteItem(stockTable.getSelectionModel().getSelectedItem().getId());
        } catch (DataAccessException e) {
            new CommonCalls().openErrorDialog(e.getMessage());
        }

        refreshTable();
    }

    @FXML
    void editStock(MouseEvent event) throws IOException {
        if (stockTable.getSelectionModel().getSelectedItem() == null ) {
            new CommonCalls().openErrorDialog("Please select an item!");
            return;
        } else if(!stockTable.getSelectionModel().getSelectedItem().getProductType().equals(StockEnums.ProductType.NON_IPOS.name())){
            new CommonCalls().openErrorDialog("Please select a Non-IPOS type!");
            return;
        }

        Stage stage = (Stage) editButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/stock/EditStock.fxml"));
        Parent root = loader.load();

        EditController controller = loader.getController();
        controller.receiver(stockTable.getSelectionModel().getSelectedItem());

        stage.setTitle("Edit item: " + stockTable.getSelectionModel().getSelectedItem().getName());
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    void rateChange(MouseEvent event) throws IOException {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/stock/RateCustomisation.fxml", "Stock");

    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/stock/StockMenu.fxml", "Stock Portal");
    }
}
