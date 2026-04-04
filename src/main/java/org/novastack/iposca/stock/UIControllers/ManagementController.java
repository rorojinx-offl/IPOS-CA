package org.novastack.iposca.stock.UIControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ManagementController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/stock/StockMenu.fxml", "Stock");
    }
}
