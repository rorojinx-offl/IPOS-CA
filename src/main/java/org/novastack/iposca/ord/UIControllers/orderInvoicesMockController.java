package org.novastack.iposca.ord.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JRException;
import org.novastack.iposca.PDF;
import org.novastack.iposca.ord.factory.OrderInvoiceReportFactory;
import org.novastack.iposca.ord.services.MockOrderSession;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class orderInvoicesMockController {
    private static final String MOCK_DB_URL = "jdbc:sqlite:ipos-sa-mock-database.db";

    @FXML
    private Button loadInvoicesButton;

    @FXML
    private Button exportPdfButton;

    @FXML
    private Button viewDetailsButton;

    @FXML
    private Button backButton;

    @FXML
    private TableView<InvoiceRow> invoicesTable;

    @FXML
    private TableColumn<InvoiceRow, String> invoiceNoColumn;

    @FXML
    private TableColumn<InvoiceRow, String> orderNoColumn;

    @FXML
    private TableColumn<InvoiceRow, Integer> merchantIdColumn;

    @FXML
    private TableColumn<InvoiceRow, String> issuedAtColumn;

    @FXML
    private TableColumn<InvoiceRow, Double> subtotalColumn;

    @FXML
    private TableColumn<InvoiceRow, Double> vatColumn;

    @FXML
    private TableColumn<InvoiceRow, Double> grandTotalColumn;

    @FXML
    private Label warning;

    @FXML
    public void initialize() {
        invoiceNoColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));
        orderNoColumn.setCellValueFactory(new PropertyValueFactory<>("orderNo"));
        merchantIdColumn.setCellValueFactory(new PropertyValueFactory<>("merchantId"));
        issuedAtColumn.setCellValueFactory(new PropertyValueFactory<>("issuedAt"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        vatColumn.setCellValueFactory(new PropertyValueFactory<>("vat"));
        grandTotalColumn.setCellValueFactory(new PropertyValueFactory<>("grandTotal"));
    }

    @FXML
    void loadInvoices(ActionEvent event) {
        Integer merchantId = MockOrderSession.getMerchantId();
        if (merchantId == null) {
            warning.setText("No active mock session. Please login again.");
            invoicesTable.getItems().clear();
            return;
        }

        String sql = """
                SELECT invoice_no, order_no, merchant_ID, issued_at, subtotal, vat, grand_total
                FROM ord_invoices
                WHERE merchant_ID = ?
                ORDER BY issued_at DESC
                """;

        ObservableList<InvoiceRow> rows = FXCollections.observableArrayList();
        try (Connection connection = DriverManager.getConnection(MOCK_DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, merchantId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new InvoiceRow(
                            rs.getString("invoice_no"),
                            rs.getString("order_no"),
                            rs.getInt("merchant_ID"),
                            rs.getString("issued_at"),
                            rs.getDouble("subtotal"),
                            rs.getDouble("vat"),
                            rs.getDouble("grand_total")
                    ));
                }
            }
            invoicesTable.setItems(rows);
            warning.setText("");
        } catch (SQLException e) {
            warning.setText("Unable to load invoices.");
        }
    }

    @FXML
    void exportPdf(ActionEvent event) {
        InvoiceRow selected = invoicesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            warning.setText("Select an invoice first.");
            return;
        }

        try {
            MerchantRow merchant = fetchMerchant(selected.getMerchantId());
            if (merchant == null) {
                warning.setText("Merchant details not found for selected invoice.");
                return;
            }

            List<OrderInvoiceReportFactory.InvoiceLine> lines = fetchInvoiceLines(selected.getOrderNo());
            if (lines.isEmpty()) {
                warning.setText("Selected invoice has no order line data.");
                return;
            }
            OrderInvoiceReportFactory.InvoiceHeader header = new OrderInvoiceReportFactory.InvoiceHeader(
                    selected.getInvoiceNo(),
                    selected.getOrderNo(),
                    selected.getIssuedAt(),
                    selected.getMerchantId(),
                    merchant.merchantName(),
                    merchant.username(),
                    merchant.accountStatus(),
                    selected.getSubtotal(),
                    selected.getVat(),
                    selected.getGrandTotal()
            );

            File pdf = OrderInvoiceReportFactory.generateInvoicePdf(header, lines);
            PDF.openPDF(pdf);
            new CommonCalls().openInfoDialog("Invoice exported successfully to: " + pdf.getPath());
            warning.setText("");
        } catch (SQLException e) {
            warning.setText("Unable to read invoice data for PDF export.");
        } catch (IOException | JRException e) {
            warning.setText("Unable to export invoice PDF.");
        }
    }

    @FXML
    void viewInvoiceDetails(ActionEvent event) {
        InvoiceRow selected = invoicesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            warning.setText("Select an invoice first.");
            return;
        }

        String sql = """
                SELECT item_id, description, quantity, unit_cost, total
                FROM ord_order_lines
                WHERE order_no = ?
                ORDER BY item_id
                """;

        StringBuilder details = new StringBuilder();
        details.append("Invoice No: ").append(selected.getInvoiceNo()).append(System.lineSeparator());
        details.append("Order No: ").append(selected.getOrderNo()).append(System.lineSeparator());
        details.append("Issued At: ").append(selected.getIssuedAt()).append(System.lineSeparator());
        details.append("Merchant ID: ").append(selected.getMerchantId()).append(System.lineSeparator());
        details.append(System.lineSeparator());
        details.append("Items").append(System.lineSeparator());
        details.append("Item ID | Description | Quantity | Unit Cost | Total").append(System.lineSeparator());

        try (Connection connection = DriverManager.getConnection(MOCK_DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, selected.getOrderNo());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    details.append(rs.getString("item_id")).append(" | ")
                            .append(rs.getString("description")).append(" | ")
                            .append(rs.getInt("quantity")).append(" | ")
                            .append(String.format("%.2f", rs.getDouble("unit_cost"))).append(" | ")
                            .append(String.format("%.2f", rs.getDouble("total")))
                            .append(System.lineSeparator());
                }
            }

            details.append(System.lineSeparator());
            details.append("Subtotal: ").append(String.format("%.2f", selected.getSubtotal())).append(System.lineSeparator());
            details.append("VAT: ").append(String.format("%.2f", selected.getVat())).append(System.lineSeparator());
            details.append("Grand Total: ").append(String.format("%.2f", selected.getGrandTotal())).append(System.lineSeparator());

            TextArea textArea = new TextArea(details.toString());
            textArea.setWrapText(true);
            textArea.setEditable(false);
            textArea.setPrefWidth(700);
            textArea.setPrefHeight(450);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Invoice Details");
            alert.setHeaderText("Invoice " + selected.getInvoiceNo());
            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
            warning.setText("");
        } catch (SQLException e) {
            warning.setText("Unable to load invoice details.");
        }
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

    public static class InvoiceRow {
        private final String invoiceNo;
        private final String orderNo;
        private final int merchantId;
        private final String issuedAt;
        private final double subtotal;
        private final double vat;
        private final double grandTotal;

        public InvoiceRow(String invoiceNo, String orderNo, int merchantId, String issuedAt,
                          double subtotal, double vat, double grandTotal) {
            this.invoiceNo = invoiceNo;
            this.orderNo = orderNo;
            this.merchantId = merchantId;
            this.issuedAt = issuedAt;
            this.subtotal = subtotal;
            this.vat = vat;
            this.grandTotal = grandTotal;
        }

        public String getInvoiceNo() {
            return invoiceNo;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public int getMerchantId() {
            return merchantId;
        }

        public String getIssuedAt() {
            return issuedAt;
        }

        public double getSubtotal() {
            return subtotal;
        }

        public double getVat() {
            return vat;
        }

        public double getGrandTotal() {
            return grandTotal;
        }
    }

    private MerchantRow fetchMerchant(int merchantId) throws SQLException {
        String sql = """
                SELECT merchant_name, username, account_status
                FROM ord_merchants
                WHERE merchant_id = ?
                LIMIT 1
                """;

        try (Connection connection = DriverManager.getConnection(MOCK_DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, merchantId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new MerchantRow(
                            rs.getString("merchant_name"),
                            rs.getString("username"),
                            rs.getString("account_status")
                    );
                }
            }
        }

        return null;
    }

    private List<OrderInvoiceReportFactory.InvoiceLine> fetchInvoiceLines(String orderNo) throws SQLException {
        String sql = """
                SELECT item_id, description, quantity, unit_cost, total
                FROM ord_order_lines
                WHERE order_no = ?
                ORDER BY item_id
                """;

        List<OrderInvoiceReportFactory.InvoiceLine> lines = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(MOCK_DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, orderNo);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    lines.add(new OrderInvoiceReportFactory.InvoiceLine(
                            rs.getString("item_id"),
                            rs.getString("description"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_cost"),
                            rs.getDouble("total")
                    ));
                }
            }
        }
        return lines;
    }

    private record MerchantRow(String merchantName, String username, String accountStatus) {
    }
}
