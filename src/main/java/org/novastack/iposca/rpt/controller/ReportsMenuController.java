package org.novastack.iposca.rpt.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.novastack.iposca.utils.ui.CommonCalls;
import org.novastack.iposca.utils.ui.ControllerTemplate;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class ReportsMenuController extends ControllerTemplate{
    @FXML
    private Button turnoverButton;

    @FXML
    private Button stockButton;

    @FXML
    private Button debtButton;

    @FXML
    private Button backButton;

    private String currentUserRole = "MANAGER";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!currentUserRole.equals("MANAGER") && !currentUserRole.equals("ADMIN")) {
            try {
                new CommonCalls().openErrorDialog("Access Denied: Only Managers and Admins can access reports.");
                goBack();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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
        new CommonCalls().traverse(stage, "/ui/cust/custMenu.fxml", "Customer Portal");
    }

}
