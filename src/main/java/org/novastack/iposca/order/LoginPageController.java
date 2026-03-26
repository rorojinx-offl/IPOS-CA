package org.novastack.iposca.order;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.jooq.exception.IOException;

public class LoginPageController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    void handleLogin(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Attempted!");
        alert.setHeaderText(null);
        alert.setContentText("Button was clicked!");
        alert.showAndWait();
    }
}