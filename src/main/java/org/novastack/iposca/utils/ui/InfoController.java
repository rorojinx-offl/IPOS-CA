package org.novastack.iposca.utils.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class InfoController {
    @FXML
    private TextArea infoMsg;

    public void setInfo(String info) {
        infoMsg.setText(info);
    }
}











