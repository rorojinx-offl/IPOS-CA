package org.novastack.iposca.order.controllers;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.jooq.exception.IOException;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoggedInController implements Initializable {

    @FXML
    private Text loggedInUsername;

    @FXML
    private Button logoutButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setLoggedInUsername(String username) {
        loggedInUsername.setText(username);
    }

    @FXML
    void handleLogout(ActionEvent event) throws IOException, java.io.IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/order/loginPage.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
