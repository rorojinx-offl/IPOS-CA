package org.novastack.iposca.stock.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.novastack.iposca.stock.Stock;
import org.novastack.iposca.stock.report.LowStockBean;
import org.novastack.iposca.stock.report.LowStockReportFactory;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lowStockObservableList.setAll(Stock.getLowStock());
        lowStockList.setItems(lowStockObservableList);
        setUpListView();
    }

    public void setUpListView(){
        lowStockList.setCellFactory(list -> new ListCell<Stock>() {
            @Override
            public void updateItem(Stock item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null){
                    setText(null);
                } else{
                    setText(item.getName());
                }
            }
        });
    }

    @FXML
    private ListView<Stock> lowStockList;
    private ObservableList<Stock> lowStockObservableList= FXCollections.observableArrayList();

    @FXML
    private VBox manageButton;

    @FXML
    void highlight(MouseEvent event) {
    }

    @FXML
    void manageStock(MouseEvent event) throws IOException {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/stock/StockManagement.fxml", "Stock");
    }

    @FXML
    void restoreCol(MouseEvent event) {
    }

    @FXML
    void genReport(MouseEvent event) throws IOException {
        ArrayList<Stock> list = new ArrayList<>(lowStockObservableList);
        ArrayList<LowStockBean> beans = new ArrayList<>();

        for (Stock stock : list){
            beans.add(new LowStockBean(
                    stock.getId(),
                    stock.getName(),
                    stock.getQuantity(),
                    stock.getStockLimit(),
                    calculateMinOrder(stock)
                    ));
        }

        LowStockReportFactory.Merchant merchant = new LowStockReportFactory.Merchant(
                "MeowMeow Pharma",
                "67 Test Avenue, Test Town, Testshire, TE1 1ST",
                "hetal@mpharma.co.uk",
                loadLogo()
        );

        LowStockReportFactory.ReportData reportData = new LowStockReportFactory.ReportData(beans, merchant);

        try {
            LowStockReportFactory.generateLowStockReport(reportData);
        } catch(Exception e) {
            new CommonCalls().openErrorDialog(e.getMessage());
        }
    }

    private int calculateMinOrder(Stock stock) {
        double multiplier = 1.1;
        double x = stock.getStockLimit() * multiplier;
        double difference = x - stock.getQuantity();
        return Math.toIntExact(Math.round(difference));
    }

    private byte[] loadLogo() throws IOException {
        try (InputStream in = MenuController.class.getResourceAsStream("/ui/stock/assets/stockManagement.jpeg")) {
            if (in == null) {
                throw new IOException("Unable to load image from file");
            }
            return in.readAllBytes();
        }
    }
}


