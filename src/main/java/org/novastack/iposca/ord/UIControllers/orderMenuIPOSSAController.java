package org.novastack.iposca.ord.UIControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.novastack.iposca.ord.services.OrderSaFacade;
import org.novastack.iposca.ord.services.RealOrderSession;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;

public class orderMenuIPOSSAController {
    private final OrderSaFacade facade = new OrderSaFacade();

    @FXML
    private Button backButton;

    @FXML
    private Label warning;

    @FXML
    void openCreateOrder(ActionEvent event) {
        navigate("/ui/ord/orderCreateIPOSSA.fxml", "Create Order (IPOS-SA)", "Unable to open create order page.");
    }

    @FXML
    void openOrderStatus(ActionEvent event) {
        navigate("/ui/ord/checkOrderStatusIPOSSA.fxml", "Check Order Status (IPOS-SA)", "Unable to open order status page.");
    }

    @FXML
    void openInvoices(ActionEvent event) {
        navigate("/ui/ord/orderInvoicesIPOSSA.fxml", "Invoices (IPOS-SA)", "Unable to open invoices page.");
    }

    @FXML
    void openBalance(ActionEvent event) {
        try {
            var balance = facade.getBalance();
            String merchantId = RealOrderSession.getMerchantId();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Outstanding Balance");
            alert.setHeaderText("Merchant: " + merchantId);
            alert.setContentText("Balance: " + balance.balance() + "\nAccount Status: " + balance.status());
            alert.showAndWait();
            warning.setText("");
        } catch (Exception e) {
            warning.setText(e.getMessage() == null ? "Unable to load merchant balance." : e.getMessage());
        }
    }

    @FXML
    void back(ActionEvent event) {
        try {
            RealOrderSession.clear();
            Stage stage = (Stage) backButton.getScene().getWindow();
            new CommonCalls().traverse(stage, "/ui/ord/orderModeSelect.fxml", "Order Mode Select");
        } catch (IOException e) {
            warning.setText("Unable to return to mode select.");
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
}
