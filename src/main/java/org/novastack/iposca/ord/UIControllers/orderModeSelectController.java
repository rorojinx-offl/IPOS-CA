package org.novastack.iposca.ord.UIControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;

public class orderModeSelectController {
    @FXML
    private Button backButton;

    @FXML
    private Button saLoginButton;

    @FXML
    private Button mockLoginButton;

    @FXML
    private Label warning;

    @FXML
    void selectSaMode(ActionEvent event) {
        openPage(saLoginButton, "/ui/ord/orderLoginIPOSSA.fxml", "Order Login (IPOS-SA)");
    }

    @FXML
    void selectMockMode(ActionEvent event) {
        openPage(mockLoginButton, "/ui/ord/orderLoginMock.fxml", "Order Login (Mock)");
    }

    @FXML
    void back(ActionEvent event) {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            new CommonCalls().traverse(stage, "/ui/dashboard/dashboard.fxml", "IPOS-CA Dashboard");
        } catch (IOException e) {
            warning.setText("Unable to return to dashboard.");
        }
    }

    private void openPage(Button sourceButton, String fxmlPath, String title) {
        try {
            Stage stage = (Stage) sourceButton.getScene().getWindow();
            new CommonCalls().traverse(stage, fxmlPath, title);
        } catch (IOException e) {
            warning.setText("Unable to open selected login page.");
        }
    }
}
