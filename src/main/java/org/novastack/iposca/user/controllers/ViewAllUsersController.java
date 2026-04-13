package org.novastack.iposca.user.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.novastack.iposca.user.User;
import org.novastack.iposca.user.UserEnums;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ViewAllUsersController implements Initializable {

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Number> idColumn;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> fullNameColumn;

    @FXML
    private TableColumn<User, LocalDate> createdAtColumn;

    @FXML
    private Label messageLabel;

    @FXML
    private ComboBox<UserEnums.UserRole> roleComboBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole().name()));
        fullNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));
        createdAtColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreatedAt()));
        roleComboBox.getItems().setAll(UserEnums.UserRole.values());
        loadUsers();
    }

    private void loadUsers() {
        try {
            usersTable.setItems(FXCollections.observableArrayList(User.getAllUsers()));
            messageLabel.setText("");
        } catch (Exception e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    void handleDeleteUser(MouseEvent event) {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            messageLabel.setText("Please select a user to delete.");
            return;
        }

        try {
            User.deleteUserById(selectedUser.getId());
            loadUsers();
            messageLabel.setText("User deleted successfully.");
        } catch (Exception e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    void handleUpdateRole(MouseEvent event) {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        UserEnums.UserRole selectedRole = roleComboBox.getValue();

        if (selectedUser == null) {
            messageLabel.setText("Please select a user to update.");
            return;
        }

        if (selectedRole == null) {
            messageLabel.setText("Please select a new role.");
            return;
        }

        try {
            User.updateUserRole(selectedUser.getId(), selectedRole);
            loadUsers();
            usersTable.getSelectionModel().clearSelection();
            roleComboBox.getSelectionModel().clearSelection();
            messageLabel.setText("User role updated successfully.");
        } catch (Exception e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) usersTable.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/user/userMenu.fxml", "Admin Dashboard");
    }
}
