package org.novastack.iposca.ord.UIControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

public class orderMenuMockController {
    private static final String MOCK_DB_URL = "jdbc:sqlite:ipos-sa-mock-database.db";

    @FXML
    private Button backButton;

    @FXML
    private Button createOrderButton;

    @FXML
    private Button statusButton;

    @FXML
    private Button balanceButton;

    @FXML
    private Button invoiceButton;

    @FXML
    private Button generateCatalogueButton;

    @FXML
    private TableView<CatalogueRow> catalogueTable;

    @FXML
    private TableColumn<CatalogueRow, String> itemIdColumn;

    @FXML
    private TableColumn<CatalogueRow, String> descriptionColumn;

    @FXML
    private TableColumn<CatalogueRow, String> packageTypeColumn;

    @FXML
    private TableColumn<CatalogueRow, String> unitColumn;

    @FXML
    private TableColumn<CatalogueRow, Integer> unitsInPackColumn;

    @FXML
    private TableColumn<CatalogueRow, Double> packageCostColumn;

    @FXML
    private TableColumn<CatalogueRow, Integer> availabilityColumn;

    @FXML
    private TableColumn<CatalogueRow, Integer> stockLimitColumn;

    @FXML
    private Label warning;

    @FXML
    public void initialize() {
        itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        packageTypeColumn.setCellValueFactory(new PropertyValueFactory<>("packageType"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitsInPackColumn.setCellValueFactory(new PropertyValueFactory<>("unitsInPack"));
        packageCostColumn.setCellValueFactory(new PropertyValueFactory<>("packageCost"));
        availabilityColumn.setCellValueFactory(new PropertyValueFactory<>("availabilityPacks"));
        stockLimitColumn.setCellValueFactory(new PropertyValueFactory<>("stockLimitPacks"));
    }

    @FXML
    void back(ActionEvent event) {
        navigate("/ui/ord/orderLoginMock.fxml", "Order Login (Mock)", "Unable to return to mock login.");
    }

    @FXML
    void openCreateOrder(ActionEvent event) {
        navigate("/ui/ord/orderCreateMock.fxml", "Create Order (Mock)", "Unable to open create order page.");
    }

    @FXML
    void openOrderStatus(ActionEvent event) {
        navigate("/ui/ord/checkOrderStatusMock.fxml", "Check Order Status (Mock)", "Unable to open order status page.");
    }

    @FXML
    void openBalance(ActionEvent event) {
        Integer merchantId = MockOrderSession.getMerchantId();
        if (merchantId == null) {
            warning.setText("No active mock session. Please login again.");
            return;
        }

        String sql = """
                SELECT outstanding_amount, updated_at
                FROM ord_balance
                WHERE merchant_id = ?
                LIMIT 1
                """;

        try (Connection connection = DriverManager.getConnection(MOCK_DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, merchantId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    double amount = rs.getDouble("outstanding_amount");
                    String updatedAt = rs.getString("updated_at");

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Outstanding Balance");
                    alert.setHeaderText("Merchant: " + MockOrderSession.getMerchantName());
                    alert.setContentText("Outstanding Balance: GBP " + String.format("%.2f", amount)
                            + "\nLast Updated: " + updatedAt);
                    alert.showAndWait();
                    warning.setText("");
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Outstanding Balance");
                    alert.setHeaderText("No balance record found");
                    alert.setContentText("No outstanding balance is available for this merchant.");
                    alert.showAndWait();
                }
            }
        } catch (SQLException e) {
            warning.setText("Unable to load outstanding balance.");
        }
    }

    @FXML
    void openInvoices(ActionEvent event) {
        navigate("/ui/ord/orderInvoicesMock.fxml", "Invoices (Mock)", "Unable to open invoices page.");
    }

    @FXML
    void generateCatalogue(ActionEvent event) {
        String sql = """
                SELECT item_id, description, package_type, unit, units_in_pack, package_cost, availability_packs, stock_limit_packs
                FROM ord_catalogue
                ORDER BY item_id
                """;

        catalogueTable.getItems().clear();

        try (Connection connection = DriverManager.getConnection(MOCK_DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                CatalogueRow row = new CatalogueRow(
                        rs.getString("item_id"),
                        rs.getString("description"),
                        rs.getString("package_type"),
                        rs.getString("unit"),
                        rs.getInt("units_in_pack"),
                        rs.getDouble("package_cost"),
                        rs.getInt("availability_packs"),
                        rs.getInt("stock_limit_packs")
                );
                catalogueTable.getItems().add(row);
            }

            warning.setText("");
        } catch (SQLException e) {
            warning.setText("Unable to load catalogue from mock database.");
        }
    }

    private void navigate(String fxmlPath, String title, String fallbackMessage) {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            new CommonCalls().traverse(stage, fxmlPath, title);
        } catch (IOException e) {
            warning.setText(fallbackMessage);
        }
    }

    public static class CatalogueRow {
        private final String itemId;
        private final String description;
        private final String packageType;
        private final String unit;
        private final int unitsInPack;
        private final double packageCost;
        private final int availabilityPacks;
        private final int stockLimitPacks;

        public CatalogueRow(String itemId, String description, String packageType, String unit,
                            int unitsInPack, double packageCost, int availabilityPacks, int stockLimitPacks) {
            this.itemId = itemId;
            this.description = description;
            this.packageType = packageType;
            this.unit = unit;
            this.unitsInPack = unitsInPack;
            this.packageCost = packageCost;
            this.availabilityPacks = availabilityPacks;
            this.stockLimitPacks = stockLimitPacks;
        }

        public String getItemId() {
            return itemId;
        }

        public String getDescription() {
            return description;
        }

        public String getPackageType() {
            return packageType;
        }

        public String getUnit() {
            return unit;
        }

        public int getUnitsInPack() {
            return unitsInPack;
        }

        public double getPackageCost() {
            return packageCost;
        }

        public int getAvailabilityPacks() {
            return availabilityPacks;
        }

        public int getStockLimitPacks() {
            return stockLimitPacks;
        }
    }
}
