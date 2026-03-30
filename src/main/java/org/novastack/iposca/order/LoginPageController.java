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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginPageController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    void handleLogin(ActionEvent event) throws IOException, java.io.IOException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login Error", "Username and password cannot be empty.");
            return;
        }

        User userService = new User();
        User foundUser = userService.getUserByUsername(username);

        if (foundUser == null) {
            showAlert("Login Failed", "Invalid username or password.");
            return;
        }

        if (foundUser.getIsActive() != 1) {
            showAlert("Login Failed", "This account is inactive.");
            return;
        }

        if (foundUser.getPassword().equals(password)) {
//            showAlert("Login Successful", "Welcome, " + foundUser.getFullName() + "!");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/order/loggedIn.fxml"));
            Parent root = loader.load();

            LoggedInController controller = loader.getController();
            controller.setLoggedInUsername(foundUser.getUsername());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();


        } else {
            showAlert("Login Failed", "Invalid username or password.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}