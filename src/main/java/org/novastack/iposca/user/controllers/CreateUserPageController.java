package org.novastack.iposca.user.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.user.User;
import org.novastack.iposca.user.UserEnums;

import javafx.scene.input.MouseEvent;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.time.LocalDate;
import java.net.URL;
import java.util.ResourceBundle;

public class CreateUserPageController implements Initializable {

    @FXML
    private TextField fullNameField;

    @FXML
    private Label messageLabel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button backButton;

    @FXML
    private Button viewAllUsersButton;

    @FXML
    private ComboBox<UserEnums.UserRole> roleComboBox;

    @FXML
    private TextField usernameField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleComboBox.getItems().setAll(UserEnums.UserRole.values());
    }

    @FXML
    void handleCreateUser(MouseEvent event) {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
        String fullName = fullNameField.getText() == null ? "" : fullNameField.getText().trim();
        UserEnums.UserRole role = roleComboBox.getValue();

        if (username.isBlank()) {
            messageLabel.setText("Username is required.");
            return;
        }

        if (password.isBlank()) {
            messageLabel.setText("Password is required.");
            return;
        }

        if (fullName.isBlank()) {
            messageLabel.setText("Full name is required.");
            return;
        }

        if (role == null) {
            messageLabel.setText("Please select a role.");
            return;
        }

        try {
            User user = new User(username, password, role, fullName, LocalDate.now());
            user.createUser(user);

            messageLabel.setText("User created successfully.");
            usernameField.clear();
            passwordField.clear();
            fullNameField.clear();
            roleComboBox.getSelectionModel().clearSelection();
        } catch (DataAccessException e) {
            String errorMessage = e.getMessage() == null ? "" : e.getMessage();
            String normalized = errorMessage.toLowerCase();

            if (normalized.contains("unique") && normalized.contains("username")) {
                messageLabel.setText("Username is already taken.");
                return;
            }

            messageLabel.setText(errorMessage);
        } catch (Exception e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/user/userMenu.fxml", "Admin Dashboard");
    }

    @FXML
    void handleViewAllUsers(MouseEvent event) throws IOException {
        Stage stage = (Stage) viewAllUsersButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/user/viewAllUsers.fxml", "Manage Users");
    }
}
