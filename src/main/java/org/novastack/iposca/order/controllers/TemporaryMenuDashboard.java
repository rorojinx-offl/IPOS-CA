package org.novastack.iposca.order.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TemporaryMenuDashboard {

    @FXML
    void handleCreateUser(ActionEvent event) throws IOException {
        navigate(event, "/ui/user/createUserPage.fxml", "Create User Account");
    }

    @FXML
    void handleMerchantLogin(ActionEvent event) throws IOException {
        navigate(event, "/ui/order/loginPage.fxml", "Merchant Login");
    }

    private void navigate(ActionEvent event, String resourcePath, String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(resourcePath));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
    }
}
