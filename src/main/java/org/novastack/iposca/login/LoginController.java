package org.novastack.iposca.login;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.controlsfx.control.textfield.CustomPasswordField;
import org.controlsfx.control.textfield.CustomTextField;
import org.novastack.iposca.exceptions.AuthenticationException;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    private CustomPasswordField pwdHidden;

    @FXML
    private CustomTextField pwdNaked;

    @FXML
    private CheckBox showPwd;

    @FXML
    private CustomTextField username;

    @FXML
    private Label warning;

    @FXML
    void togglePassword(ActionEvent event) {
        if (showPwd.isSelected()) {
            pwdHidden.setVisible(false);
            pwdHidden.setManaged(false);

            pwdNaked.setText(pwdHidden.getText());
            pwdNaked.setVisible(true);
            pwdNaked.setManaged(true);
            return;
        }
        pwdNaked.setVisible(false);
        pwdNaked.setManaged(false);

        pwdHidden.setText(pwdNaked.getText());
        pwdHidden.setVisible(true);
        pwdHidden.setManaged(true);
    }

    @FXML
    void loginFromPwdField(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            login(null);
        }
    }

    @FXML
    void login(MouseEvent event) {
        boolean allFieldsFilled = true;
        String uname = username.getText();
        String pwd = pwdHidden.getText();

        if (uname.isEmpty()) {
            warning.setText("Username cannot be empty!");
            allFieldsFilled = false;
        }
        if (pwd.isEmpty() && pwdNaked.getText().isEmpty()) {
            warning.setText("Password cannot be empty!");
            allFieldsFilled = false;
        }
        if (uname.isEmpty() && (pwd.isEmpty() && pwdNaked.getText().isEmpty())) {
            warning.setText("Username and password cannot be empty!");
            allFieldsFilled = false;
        }

        if (allFieldsFilled) {
            try {
                Login.login(uname, pwd.isEmpty() ? pwdNaked.getText() : pwd, username);
            } catch (Exception e) {
                if (e instanceof AuthenticationException) {
                    warning.setText(e.getMessage());
                } else if (e instanceof IOException) {
                    warning.setText("Display Error!");
                } else {
                    warning.setText("Unknown Error!");
                }
            }
        }
    }

}
