package org.novastack.iposca.utils.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ErrorController {
    @FXML
    private TextArea errorMsg;

    public void setError(String error) {
        errorMsg.setText(error);
    }
}
