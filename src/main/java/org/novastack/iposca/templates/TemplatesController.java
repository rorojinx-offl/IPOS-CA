package org.novastack.iposca.templates;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.novastack.iposca.config.AppConfig;
import org.novastack.iposca.config.AppConfigAPI;
import org.novastack.iposca.utils.ui.CommonCalls;
import org.novastack.iposca.utils.ui.IValid;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TemplatesController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        getConfig();
    }

    @FXML
    private TextField address;

    @FXML
    private Label addressWarning;

    @FXML
    private Button back;

    @FXML
    private TextField email;

    @FXML
    private Label emailWarning;

    @FXML
    private ImageView logoViewer;

    @FXML
    private TextField name;

    @FXML
    private Label nameWarning;

    @FXML
    private Button save;

    private byte[] logo;

    private void clearWarnings () {
        nameWarning.setText("");
        addressWarning.setText("");
        emailWarning.setText("");
    }

    @FXML
    void save(MouseEvent event) throws IOException {
        boolean allFieldsFilled = true;
        clearWarnings();

        if (name.getText().isEmpty()) {
            nameWarning.setText("Name cannot be empty");
            allFieldsFilled = false;
        }
        if (address.getText().isEmpty()) {
            addressWarning.setText("Address cannot be empty");
            allFieldsFilled = false;
        }
        if (email.getText().isEmpty()) {
            emailWarning.setText("Email cannot be empty");
            allFieldsFilled = false;
        }
        if (!email.getText().isEmpty() && !IValid.checkEmail(email.getText())) {
            emailWarning.setText("Invalid Email");
            allFieldsFilled = false;
        }

        if (allFieldsFilled)  {
            boolean ok = new CommonCalls().openConfirmationDialog("Are you sure you want to save changes?");
            if (!ok) {
                return;
            }

            AppConfig ac = new AppConfig(AppConfig.ConfigKey.MERCHANT_NAME, AppConfigAPI.encodeString(name.getText()));
            ac.configure(ac);

            ac = new AppConfig(AppConfig.ConfigKey.MERCHANT_ADDRESS, AppConfigAPI.encodeString(address.getText()));
            ac.configure(ac);

            ac = new AppConfig(AppConfig.ConfigKey.MERCHANT_EMAIL, AppConfigAPI.encodeString(email.getText()));
            ac.configure(ac);

            ac = new AppConfig(AppConfig.ConfigKey.MERCHANT_LOGO, logo);
            ac.configure(ac);
        }
    }

    @FXML
    void uploadImage(MouseEvent event) throws IOException {
        Stage stage = (Stage) save.getScene().getWindow();
        ImageUtils.chooseImage(stage, logoViewer);
        try{
            logo = ImageUtils.getImageBytes();
        }catch (Exception e){
            new CommonCalls().openErrorDialog("Unable to upload Image: " + e.getMessage());
        }
    }

    @FXML
    void back(MouseEvent event) throws IOException {
        Stage stage = (Stage) back.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/dashboard/dashboard.fxml", "IPOS-CA Dashboard");
    }

    private void getConfig(){
        //Get config only if it isn't corrupted
        if(AppConfig.configExists()){
            name.setText(AppConfigAPI.decodeByteToString(AppConfig.get(AppConfig.ConfigKey.MERCHANT_NAME)));
            address.setText(AppConfigAPI.decodeByteToString(AppConfig.get(AppConfig.ConfigKey.MERCHANT_ADDRESS)));
            email.setText(AppConfigAPI.decodeByteToString(AppConfig.get(AppConfig.ConfigKey.MERCHANT_EMAIL)));
            byte[] logo = AppConfig.get(AppConfig.ConfigKey.MERCHANT_LOGO);
            ImageUtils.loadImage(logo, logoViewer);
            this.logo = logo;
        }
    }

}
