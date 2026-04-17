package org.novastack.iposca.user.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }


    @FXML
    private VBox addButton;

    @FXML
    private Button backButton;

    @FXML
    private VBox mgmtButton;

    @FXML
    void createUser(MouseEvent event) throws IOException {
        Stage stage = (Stage) addButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/user/createUserPage.fxml", "Create a User");
    }

    @FXML
    void highlight(MouseEvent event) {
        VBox option = (VBox) event.getSource();
        Label childLabel = (Label) option.getChildren().get(1);
        childLabel.setTextFill(Color.RED);
        option.setCursor(Cursor.HAND);
    }

    @FXML
    void manageUsers(MouseEvent event) throws IOException {
        Stage stage = (Stage) mgmtButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/user/viewAllUsers.fxml", "Manage Users");
    }

    @FXML
    void restoreCol(MouseEvent event) {
        VBox option = (VBox) event.getSource();
        Label childLabel = (Label) option.getChildren().get(1);
        childLabel.setTextFill(Color.BLACK);
        option.setCursor(Cursor.DEFAULT);
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/dashboard/dashboard.fxml", "IPOS-CA Dashboard");
    }

}
