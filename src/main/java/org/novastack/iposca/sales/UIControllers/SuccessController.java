package org.novastack.iposca.sales.UIControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SuccessController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    private Button okButton;

    @FXML
    void printInvoice(MouseEvent event) {

    }

    @FXML
    void returnToMenu(MouseEvent event) throws IOException {
        new CommonCalls().traverse((Stage) okButton.getScene().getWindow(), "/ui/sales/salesMenu.fxml", "Sales");
    }
}
