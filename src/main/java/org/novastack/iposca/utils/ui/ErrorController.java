package org.novastack.iposca.utils.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ErrorController {
    @FXML
    private TextField errorMsg;

    public void setError(String error) {
        errorMsg.setText(error);
    }
}
