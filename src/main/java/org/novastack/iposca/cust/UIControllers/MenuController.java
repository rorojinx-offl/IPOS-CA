package org.novastack.iposca.cust.UIControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    private VBox regButton;

    @FXML
    private VBox debtButton;

    @FXML
    private VBox manageButton;


    @FXML
    void registerCustomer(MouseEvent event) throws IOException {
        Stage stage = (Stage) regButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/cust/custReg.fxml", "Customer Registration");
    }

    @FXML
    void manageCustomer(MouseEvent event) throws IOException {
        Stage stage = (Stage) regButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/cust/custMgmt.fxml", "Customer Management");
    }

    @FXML
    void manageDebt(MouseEvent event) throws IOException {
        Stage stage = (Stage) debtButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/cust/debtMgmt.fxml", "Debt Management");
    }

    @FXML
    void highlight(MouseEvent event) {
        VBox option = (VBox) event.getSource();
        Label childLabel = (Label) option.getChildren().get(1);
        childLabel.setTextFill(Color.RED);
    }

    @FXML
    void restoreCol(MouseEvent event) {
        VBox option = (VBox) event.getSource();
        Label childLabel = (Label) option.getChildren().get(1);
        childLabel.setTextFill(Color.BLACK);
    }
}
