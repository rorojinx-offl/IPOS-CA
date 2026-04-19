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
import org.novastack.iposca.ord.services.OrderSaFacade;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;

public class orderInvoicesIPOSSAController {
    private final OrderSaFacade facade = new OrderSaFacade();

    @FXML
    private Button backButton;

    @FXML
    private TableView<InvoiceRow> invoicesTable;

    @FXML
    private TableColumn<InvoiceRow, String> invoiceIdColumn;

    @FXML
    private TableColumn<InvoiceRow, String> orderIdColumn;

    @FXML
    private TableColumn<InvoiceRow, String> merchantIdColumn;

    @FXML
    private TableColumn<InvoiceRow, String> invoiceDateColumn;

    @FXML
    private TableColumn<InvoiceRow, String> amountDueColumn;

    @FXML
    private Label warning;

    @FXML
    public void initialize() {
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        merchantIdColumn.setCellValueFactory(new PropertyValueFactory<>("merchantId"));
        invoiceDateColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        amountDueColumn.setCellValueFactory(new PropertyValueFactory<>("amountDue"));
    }

    @FXML
    void loadInvoices(ActionEvent event) {
        try {
            ObservableList<InvoiceRow> rows = FXCollections.observableArrayList();
            for (var inv : facade.getInvoices()) {
                rows.add(new InvoiceRow(
                        inv.invoiceId(),
                        inv.orderId(),
                        inv.merchantId(),
                        inv.invoiceDate(),
                        inv.amountDue()
                ));
            }

            invoicesTable.setItems(rows);
            warning.setText("");
        } catch (Exception e) {
            warning.setText(e.getMessage() == null ? "Unable to load invoices." : e.getMessage());
        }
    }

    @FXML
    void viewInvoiceDetails(ActionEvent event) {
        InvoiceRow selected = invoicesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            warning.setText("Select an invoice first.");
            return;
        }

        try {
            String printable = facade.getInvoiceDetails(selected.invoiceId);

            TextArea textArea = new TextArea(printable);
            textArea.setWrapText(true);
            textArea.setEditable(false);
            textArea.setPrefWidth(700);
            textArea.setPrefHeight(500);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Invoice Details");
            alert.setHeaderText("Invoice " + selected.invoiceId);
            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
            warning.setText("");
        } catch (Exception e) {
            warning.setText(e.getMessage() == null ? "Unable to load invoice details." : e.getMessage());
        }
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

    public static class InvoiceRow {
        private final String invoiceId;
        private final String orderId;
        private final String merchantId;
        private final String invoiceDate;
        private final String amountDue;

        public InvoiceRow(String invoiceId, String orderId, String merchantId, String invoiceDate, String amountDue) {
            this.invoiceId = invoiceId;
            this.orderId = orderId;
            this.merchantId = merchantId;
            this.invoiceDate = invoiceDate;
            this.amountDue = amountDue;
        }

        public String getInvoiceId() {
            return invoiceId;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getMerchantId() {
            return merchantId;
        }

        public String getInvoiceDate() {
            return invoiceDate;
        }

        public String getAmountDue() {
            return amountDue;
        }
    }
}
