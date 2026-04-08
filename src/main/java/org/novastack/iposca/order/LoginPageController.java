package org.novastack.iposca.order;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.jooq.exception.IOException;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.novastack.iposca.order.controllers.LoggedInController;

public class LoginPageController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final LoginService loginService = new LoginService();

    @FXML
    void handleLogin(ActionEvent event) throws IOException, java.io.IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter both username and password.");
            return;
        }

        try {
            LoginResult loginResult = loginService.loginToIPOSSA(username, password);

            if (!"MERCHANT".equalsIgnoreCase(loginResult.getRole())) {
                showAlert(Alert.AlertType.ERROR, "Access Denied", "This login is not a merchant account.");
                return;
            }

            loginService.openMerchantSite(loginResult.getSessionToken());

            showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Merchant site opened in browser.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Login Failed", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}