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
import org.novastack.iposca.ord.services.MockOrderSession;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class checkOrderStatusMockController {
    private static final String MOCK_DB_URL = "jdbc:sqlite:ipos-sa-mock-database.db";

    @FXML
    private Button refreshButton;

    @FXML
    private Button backButton;

    @FXML
    private TableView<OrderStatusRow> ordersTable;

    @FXML
    private TableColumn<OrderStatusRow, String> orderNoColumn;

    @FXML
    private TableColumn<OrderStatusRow, String> orderDateColumn;

    @FXML
    private TableColumn<OrderStatusRow, String> statusColumn;

    @FXML
    private TableColumn<OrderStatusRow, Double> grandTotalColumn;

    @FXML
    private Label warning;

    @FXML
    public void initialize() {
        orderNoColumn.setCellValueFactory(new PropertyValueFactory<>("orderNo"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        grandTotalColumn.setCellValueFactory(new PropertyValueFactory<>("grandTotal"));
        loadOrdersForCurrentMerchant();
    }

    @FXML
    void refreshOrders(ActionEvent event) {
        loadOrdersForCurrentMerchant();
    }

    @FXML
    void back(ActionEvent event) {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            new CommonCalls().traverse(stage, "/ui/ord/orderMenuMock.fxml", "Order Menu (Mock)");
        } catch (IOException e) {
            warning.setText("Unable to return to order menu.");
        }
    }

    private void loadOrdersForCurrentMerchant() {
        Integer merchantId = MockOrderSession.getMerchantId();
        if (merchantId == null) {
            warning.setText("No active mock session. Please login again.");
            ordersTable.getItems().clear();
            return;
        }

        String sql = """
                SELECT order_no, order_date, status, grand_total
                FROM ord_orders
                WHERE merchant_id = ?
                ORDER BY order_date DESC
                """;

        ObservableList<OrderStatusRow> rows = FXCollections.observableArrayList();
        try (Connection connection = DriverManager.getConnection(MOCK_DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, merchantId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new OrderStatusRow(
                            rs.getString("order_no"),
                            rs.getString("order_date"),
                            rs.getString("status"),
                            rs.getDouble("grand_total")
                    ));
                }
            }
            ordersTable.setItems(rows);
            warning.setText("");
        } catch (SQLException e) {
            warning.setText("Unable to load order status.");
        }
    }

    public static class OrderStatusRow {
        private final String orderNo;
        private final String orderDate;
        private final String status;
        private final double grandTotal;

        public OrderStatusRow(String orderNo, String orderDate, String status, double grandTotal) {
            this.orderNo = orderNo;
            this.orderDate = orderDate;
            this.status = status;
            this.grandTotal = grandTotal;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public String getOrderDate() {
            return orderDate;
        }

        public String getStatus() {
            return status;
        }

        public double getGrandTotal() {
            return grandTotal;
        }
    }
}
