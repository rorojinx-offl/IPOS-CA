package org.novastack.iposca.cust.UIControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    private VBox regButton;

    @FXML
    void registerCustomer(MouseEvent event) throws IOException {
        Stage stage = (Stage) regButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/cust/custReg.fxml");
    }
}
