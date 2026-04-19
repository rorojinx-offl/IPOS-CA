package org.novastack.iposca.ord.UIControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.novastack.iposca.ord.services.OrderSaFacade;
import org.novastack.iposca.ord.services.RealOrderSession;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;

public class orderLoginIPOSSAController {
    private final OrderSaFacade facade = new OrderSaFacade();

    @FXML
    private Button backButton;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Button loginButton;

    @FXML
    private Label warning;

    @FXML
    void login(ActionEvent event) {
        String enteredEmail = username.getText().trim();
        String enteredPassword = password.getText().trim();

        if (enteredEmail.isEmpty() || enteredPassword.isEmpty()) {
            warning.setTextFill(Color.RED);
            warning.setText("Please enter email and password.");
            return;
        }

        try {
            facade.authenticate(enteredEmail, enteredPassword);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            new CommonCalls().traverse(stage, "/ui/ord/orderMenuIPOSSA.fxml", "Order Menu (IPOS-SA)");
            warning.setTextFill(Color.GREEN);
            warning.setText("Authenticated with IPOS-SA.");
        } catch (IOException | InterruptedException e) {
            warning.setTextFill(Color.RED);
            warning.setText(e.getMessage() == null ? "Unable to connect to IPOS-SA at localhost:8080." : e.getMessage());
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
}
