package org.novastack.iposca.rpt.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.novastack.iposca.session.Session;
import org.novastack.iposca.session.SessionManager;
import org.novastack.iposca.user.User;
import org.novastack.iposca.user.UserEnums;
import org.novastack.iposca.utils.ui.CommonCalls;
import org.novastack.iposca.utils.ui.ControllerTemplate;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class ReportsMenuController {
    @FXML
    private Button turnoverButton;

    @FXML
    private Button stockButton;

    @FXML
    private Button debtButton;

    @FXML
    private Button backButton;

    @FXML
    void openTurnoverReport(MouseEvent event) throws IOException {
        Stage stage = (Stage) turnoverButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/rpt/turnoverReport.fxml", "Turnover Report");
    }

    @FXML
    void openStockReport(MouseEvent event) throws IOException {
        Stage stage = (Stage) stockButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/rpt/stockReport.fxml", "Stock Availability Report");
    }

    @FXML
    void openDebtReport(MouseEvent event) throws IOException {
        Stage stage = (Stage) debtButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/rpt/debtReport.fxml", "Aggregated Debt Change Report");
    }

    @FXML
    void goBack() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/dashboard/dashboard.fxml", "IPOS-CA Dashboard");
    }

}
