package org.novastack.iposca.stock.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.novastack.iposca.config.AppConfig;
import org.novastack.iposca.config.AppConfigAPI;
import org.novastack.iposca.cust.reminders.ReminderInfo;
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
    private Button backButton;

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

        if (!AppConfig.configExists()) {
            new CommonCalls().openErrorDialog("Document Template not configured. Please contact your admin/manager to configure it.");
            return;
        }

        LowStockReportFactory.Merchant merchant = new LowStockReportFactory.Merchant(
                AppConfigAPI.decodeByteToString(AppConfig.get(AppConfig.ConfigKey.MERCHANT_NAME)),
                AppConfigAPI.decodeByteToString(AppConfig.get(AppConfig.ConfigKey.MERCHANT_ADDRESS)),
                AppConfigAPI.decodeByteToString(AppConfig.get(AppConfig.ConfigKey.MERCHANT_EMAIL)),
                AppConfig.get(AppConfig.ConfigKey.MERCHANT_LOGO));

        LowStockReportFactory.ReportData reportData = new LowStockReportFactory.ReportData(beans, merchant);

        try {
            LowStockReportFactory.generateLowStockReport(reportData);
        } catch(Exception e) {
            new CommonCalls().openErrorDialog(e.getMessage());
        }
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/dashboard/dashboard.fxml", "IPOS-CA Dashboard");
    }

    private int calculateMinOrder(Stock stock) {
        double multiplier = 1.1;
        double x = stock.getStockLimit() * multiplier;
        double difference = x - stock.getQuantity();
        return Math.toIntExact(Math.round(difference));
    }
}


