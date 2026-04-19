package org.novastack.iposca.ord.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.novastack.iposca.ord.services.OrderSaFacade;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;

public class checkOrderStatusIPOSSAController {
    private final OrderSaFacade facade = new OrderSaFacade();

    @FXML
    private Button backButton;

    @FXML
    private TableView<OrderRow> ordersTable;

    @FXML
    private TableColumn<OrderRow, String> orderIdColumn;

    @FXML
    private TableColumn<OrderRow, String> orderDateColumn;

    @FXML
    private TableColumn<OrderRow, String> statusColumn;

    @FXML
    private TableColumn<OrderRow, String> totalColumn;

    @FXML
    private Label warning;

    @FXML
    public void initialize() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        loadOrders();
    }

    @FXML
    void refreshOrders(ActionEvent event) {
        loadOrders();
    }

    @FXML
    void back(ActionEvent event) {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            new CommonCalls().traverse(stage, "/ui/ord/orderMenuIPOSSA.fxml", "Order Menu (IPOS-SA)");
        } catch (IOException e) {
            warning.setText("Unable to return to order menu.");
        }
    }

    private void loadOrders() {
        try {
            ObservableList<OrderRow> rows = FXCollections.observableArrayList();
            for (var o : facade.getOrders()) {
                rows.add(new OrderRow(o.orderId(), o.orderDate(), o.status(), o.total()));
            }
            ordersTable.setItems(rows);
            warning.setText("");
        } catch (Exception e) {
            warning.setText(e.getMessage() == null ? "Unable to load orders." : e.getMessage());
        }
    }

    public static class OrderRow {
        private final String orderId;
        private final String orderDate;
        private final String status;
        private final String total;

        public OrderRow(String orderId, String orderDate, String status, String total) {
            this.orderId = orderId;
            this.orderDate = orderDate;
            this.status = status;
            this.total = total;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getOrderDate() {
            return orderDate;
        }

        public String getStatus() {
            return status;
        }

        public String getTotal() {
            return total;
        }
    }
}
