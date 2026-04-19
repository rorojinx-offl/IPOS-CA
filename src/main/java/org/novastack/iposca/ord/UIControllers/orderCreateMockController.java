package org.novastack.iposca.ord.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.novastack.iposca.ord.services.MockOrderSession;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class orderCreateMockController {
    private static final String MOCK_DB_URL = "jdbc:sqlite:ipos-sa-mock-database.db";

    @FXML
    private Button backButton;

    @FXML
    private Button loadCatalogueButton;

    @FXML
    private Button addToOrderButton;

    @FXML
    private Button submitOrderButton;

    @FXML
    private TableView<CatalogueItemRow> catalogueTable;

    @FXML
    private TableColumn<CatalogueItemRow, String> itemIdColumn;

    @FXML
    private TableColumn<CatalogueItemRow, String> descriptionColumn;

    @FXML
    private TableColumn<CatalogueItemRow, Double> unitCostColumn;

    @FXML
    private TableColumn<CatalogueItemRow, Integer> availabilityColumn;

    @FXML
    private TableColumn<CatalogueItemRow, Integer> stockLimitColumn;

    @FXML
    private TableView<OrderLineRow> orderLinesTable;

    @FXML
    private TableColumn<OrderLineRow, String> orderItemIdColumn;

    @FXML
    private TableColumn<OrderLineRow, String> orderDescriptionColumn;

    @FXML
    private TableColumn<OrderLineRow, Integer> orderQuantityColumn;

    @FXML
    private TableColumn<OrderLineRow, Double> orderUnitCostColumn;

    @FXML
    private TableColumn<OrderLineRow, Double> orderTotalColumn;

    @FXML
    private TextField quantityField;

    @FXML
    private Label warning;

    @FXML
    public void initialize() {
        itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        unitCostColumn.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
        availabilityColumn.setCellValueFactory(new PropertyValueFactory<>("availability"));
        stockLimitColumn.setCellValueFactory(new PropertyValueFactory<>("stockLimit"));

        orderItemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        orderDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        orderQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        orderUnitCostColumn.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
        orderTotalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
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

    @FXML
    void loadCatalogue(ActionEvent event) {
        loadCatalogueRows();
    }

    private void loadCatalogueRows() {
        String sql = """
                SELECT item_id, description, package_cost, availability_packs, stock_limit_packs
                FROM ord_catalogue
                ORDER BY item_id
                """;

        ObservableList<CatalogueItemRow> rows = FXCollections.observableArrayList();

        try (Connection connection = DriverManager.getConnection(MOCK_DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                rows.add(new CatalogueItemRow(
                        rs.getString("item_id"),
                        rs.getString("description"),
                        rs.getDouble("package_cost"),
                        rs.getInt("availability_packs"),
                        rs.getInt("stock_limit_packs")
                ));
            }
            catalogueTable.setItems(rows);
            warning.setTextFill(Color.BLACK);
            warning.setText("");
        } catch (SQLException e) {
            warning.setText("Unable to load catalogue.");
        }
    }

    @FXML
    void addToOrder(ActionEvent event) {
        CatalogueItemRow selected = catalogueTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            warning.setText("Select a catalogue item first.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                warning.setText("Quantity must be greater than 0.");
                return;
            }
        } catch (Exception e) {
            warning.setText("Enter a valid quantity.");
            return;
        }

        int mergedQuantity = quantity + getExistingQuantity(selected.getItemId());
        if (mergedQuantity > selected.getAvailability()) {
            warning.setText("Quantity exceeds current availability.");
            return;
        }

        upsertOrderLine(selected, mergedQuantity);
        warning.setTextFill(Color.BLACK);
        warning.setText("");
        quantityField.clear();
    }

    @FXML
    void submitOrder(ActionEvent event) {
        if (orderLinesTable.getItems().isEmpty()) {
            warning.setText("Add at least one order line before submitting.");
            return;
        }

        Integer merchantId = MockOrderSession.getMerchantId();
        if (merchantId == null) {
            warning.setText("Session expired. Please login again.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(MOCK_DB_URL)) {
            connection.setAutoCommit(false);
            try {
                String orderNo = generateNextOrderNo(connection);
                String invoiceNo = generateNextInvoiceNo(connection);
                String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                ensureAvailability(connection);
                double grandTotal = calculateGrandTotal();
                double subtotal = roundCurrency(grandTotal / 1.2);
                double vat = roundCurrency(grandTotal - subtotal);

                insertOrder(connection, orderNo, merchantId, now, grandTotal);
                insertOrderLines(connection, orderNo);
                insertInvoice(connection, invoiceNo, orderNo, merchantId, now, subtotal, vat, grandTotal);
                updateBalance(connection, merchantId, grandTotal, now);
                updateCatalogueAvailability(connection);

                connection.commit();
                orderLinesTable.getItems().clear();
                quantityField.clear();
                loadCatalogueRows();
                warning.setTextFill(Color.GREEN);
                warning.setText("Order sent and pending approval. Order No: " + orderNo + " | Invoice No: " + invoiceNo);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            warning.setTextFill(Color.RED);
            warning.setText("Order submission failed: " + e.getMessage());
        }
    }

    private void ensureAvailability(Connection connection) throws SQLException {
        String availabilitySql = "SELECT availability_packs FROM ord_catalogue WHERE item_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(availabilitySql)) {
            for (OrderLineRow line : orderLinesTable.getItems()) {
                statement.setString(1, line.getItemId());
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Catalogue item not found: " + line.getItemId());
                    }
                    int available = rs.getInt("availability_packs");
                    if (line.getQuantity() > available) {
                        throw new SQLException("Insufficient stock for item " + line.getItemId());
                    }
                }
            }
        }
    }

    private double calculateGrandTotal() {
        double total = 0.0;
        for (OrderLineRow line : orderLinesTable.getItems()) {
            total += line.getTotal();
        }
        return roundCurrency(total);
    }

    private void insertOrder(Connection connection, String orderNo, int merchantId, String orderDate, double grandTotal)
            throws SQLException {
        String sql = """
                INSERT INTO ord_orders (order_no, merchant_id, order_date, status, grand_total)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, orderNo);
            statement.setInt(2, merchantId);
            statement.setString(3, orderDate);
            statement.setString(4, "PLACED");
            statement.setDouble(5, grandTotal);
            statement.executeUpdate();
        }
    }

    private void insertOrderLines(Connection connection, String orderNo) throws SQLException {
        String sql = """
                INSERT INTO ord_order_lines (order_no, item_id, description, quantity, unit_cost, total)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (OrderLineRow line : orderLinesTable.getItems()) {
                statement.setString(1, orderNo);
                statement.setString(2, line.getItemId());
                statement.setString(3, line.getDescription());
                statement.setInt(4, line.getQuantity());
                statement.setDouble(5, line.getUnitCost());
                statement.setDouble(6, line.getTotal());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertInvoice(Connection connection, String invoiceNo, String orderNo, int merchantId, String issuedAt,
                               double subtotal, double vat, double grandTotal) throws SQLException {
        String sql = """
                INSERT INTO ord_invoices (invoice_no, order_no, merchant_ID, issued_at, subtotal, vat, grand_total)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, invoiceNo);
            statement.setString(2, orderNo);
            statement.setInt(3, merchantId);
            statement.setString(4, issuedAt);
            statement.setDouble(5, subtotal);
            statement.setDouble(6, vat);
            statement.setDouble(7, grandTotal);
            statement.executeUpdate();
        }
    }

    private void updateBalance(Connection connection, int merchantId, double amountToAdd, String updatedAt)
            throws SQLException {
        String updateSql = """
                UPDATE ord_balance
                SET outstanding_amount = outstanding_amount + ?, updated_at = ?
                WHERE merchant_id = ?
                """;
        try (PreparedStatement update = connection.prepareStatement(updateSql)) {
            update.setDouble(1, amountToAdd);
            update.setString(2, updatedAt);
            update.setInt(3, merchantId);
            int rows = update.executeUpdate();

            if (rows == 0) {
                String insertSql = """
                        INSERT INTO ord_balance (merchant_id, outstanding_amount, updated_at)
                        VALUES (?, ?, ?)
                        """;
                try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                    insert.setInt(1, merchantId);
                    insert.setDouble(2, amountToAdd);
                    insert.setString(3, updatedAt);
                    insert.executeUpdate();
                }
            }
        }
    }

    private void updateCatalogueAvailability(Connection connection) throws SQLException {
        String sql = """
                UPDATE ord_catalogue
                SET availability_packs = availability_packs - ?
                WHERE item_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (OrderLineRow line : orderLinesTable.getItems()) {
                statement.setInt(1, line.getQuantity());
                statement.setString(2, line.getItemId());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private String generateNextOrderNo(Connection connection) throws SQLException {
        int currentYear = LocalDateTime.now().getYear();
        String prefix = "ORD-" + currentYear + "-";
        int nextNumber = getNextRunningNumber(connection, "ord_orders", "order_no", prefix);
        return String.format(Locale.ROOT, "%s%04d", prefix, nextNumber);
    }

    private String generateNextInvoiceNo(Connection connection) throws SQLException {
        int currentYear = LocalDateTime.now().getYear();
        String prefix = "INV-" + currentYear + "-";
        int nextNumber = getNextRunningNumber(connection, "ord_invoices", "invoice_no", prefix);
        return String.format(Locale.ROOT, "%s%04d", prefix, nextNumber);
    }

    private int getNextRunningNumber(Connection connection, String tableName, String columnName, String prefix)
            throws SQLException {
        String sql = "SELECT " + columnName + " FROM " + tableName + " WHERE " + columnName + " LIKE ? ORDER BY "
                + columnName + " DESC LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, prefix + "%");
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String current = rs.getString(1);
                    String suffix = current.substring(prefix.length());
                    return Integer.parseInt(suffix) + 1;
                }
            }
        }
        return 1;
    }

    private int getExistingQuantity(String itemId) {
        for (OrderLineRow line : orderLinesTable.getItems()) {
            if (line.getItemId().equals(itemId)) {
                return line.getQuantity();
            }
        }
        return 0;
    }

    private void upsertOrderLine(CatalogueItemRow selected, int mergedQuantity) {
        OrderLineRow existing = null;
        int existingIndex = -1;

        for (int i = 0; i < orderLinesTable.getItems().size(); i++) {
            OrderLineRow line = orderLinesTable.getItems().get(i);
            if (line.getItemId().equals(selected.getItemId())) {
                existing = line;
                existingIndex = i;
                break;
            }
        }

        OrderLineRow merged = new OrderLineRow(
                selected.getItemId(),
                selected.getDescription(),
                mergedQuantity,
                selected.getUnitCost(),
                roundCurrency(selected.getUnitCost() * mergedQuantity)
        );

        if (existing != null) {
            orderLinesTable.getItems().set(existingIndex, merged);
        } else {
            orderLinesTable.getItems().add(merged);
        }
    }

    private double roundCurrency(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static class CatalogueItemRow {
        private final String itemId;
        private final String description;
        private final double unitCost;
        private final int availability;
        private final int stockLimit;

        public CatalogueItemRow(String itemId, String description, double unitCost, int availability, int stockLimit) {
            this.itemId = itemId;
            this.description = description;
            this.unitCost = unitCost;
            this.availability = availability;
            this.stockLimit = stockLimit;
        }

        public String getItemId() {
            return itemId;
        }

        public String getDescription() {
            return description;
        }

        public double getUnitCost() {
            return unitCost;
        }

        public int getAvailability() {
            return availability;
        }

        public int getStockLimit() {
            return stockLimit;
        }
    }

    public static class OrderLineRow {
        private final String itemId;
        private final String description;
        private final int quantity;
        private final double unitCost;
        private final double total;

        public OrderLineRow(String itemId, String description, int quantity, double unitCost, double total) {
            this.itemId = itemId;
            this.description = description;
            this.quantity = quantity;
            this.unitCost = unitCost;
            this.total = total;
        }

        public String getItemId() {
            return itemId;
        }

        public String getDescription() {
            return description;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getUnitCost() {
            return unitCost;
        }

        public double getTotal() {
            return total;
        }
    }
}
