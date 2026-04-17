package org.novastack.iposca.utils.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ConfirmController {
    private boolean result = false;

    public boolean getResult() {
        return result;
    }

    public void setPrompt(String prompt) {
        this.prompt.setText(prompt);
    }

    @FXML
    private Button button;

    @FXML
    private Label prompt;


    @FXML
    void cancel(MouseEvent event) {
        result = false;
        close();
    }

    @FXML
    void confirm(MouseEvent event) {
        result = true;
        close();
    }

    private void close() {
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }
}
