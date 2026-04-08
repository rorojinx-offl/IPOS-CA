package org.novastack.iposca.order.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.novastack.iposca.user.UserEnums;

public class CreateUserPageController {

    @FXML
    private TextField fullNameField;

    @FXML
    private Label messageLabel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button returnDashboard;

    @FXML
    private ComboBox<UserEnums.UserRole> roleComboBox;

    @FXML
    private TextField usernameField;

    @FXML
    void handleCreateUser(ActionEvent event) {

    }
}
