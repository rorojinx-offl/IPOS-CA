package org.novastack.iposca.templates.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.novastack.iposca.templates.model.ShopIdentity;
import org.novastack.iposca.templates.service.ShopIdentityService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ShopIdentityController {

    @FXML
    private TextField pharmacyNameField;

    @FXML
    private TextArea addressArea;

    @FXML
    private TextField emailField;

    @FXML
    private TextField logoPathField;

    private final ShopIdentityService service = new ShopIdentityService();
    private String currentUser = "admin"; // TODO: Get from actual session

    /**
     * Initialize method - called after FXML is loaded
     */
    @FXML
    public void initialize() {
        loadExistingIdentity();
    }

    /**
     * Load existing shop identity from database
     */
    private void loadExistingIdentity() {
        try {
            ShopIdentity identity = service.loadIdentity();
            if (identity != null) {
                pharmacyNameField.setText(identity.getPharmacyName());
                addressArea.setText(identity.getAddress());
                emailField.setText(identity.getEmail());
                if (identity.getLogoPath() != null) {
                    logoPathField.setText(identity.getLogoPath());
                }
            }
        } catch (Exception e) {
            showError("Failed to load shop identity: " + e.getMessage());
        }
    }

    /**
     * Browse for logo image file
     */
    @FXML
    public void browseLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Logo Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            // Validate file size (max 2MB)
            if (selectedFile.length() > 2 * 1024 * 1024) {
                showError("Logo file must be less than 2MB");
                return;
            }

            // Copy to project directory
            try {
                String fileName = "logo_" + System.currentTimeMillis() + "_" + selectedFile.getName();
                Path destination = Path.of("logos", fileName);
                Files.createDirectories(destination.getParent());
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                logoPathField.setText(destination.toString());
                showSuccess("Logo selected successfully");
            } catch (Exception e) {
                showError("Failed to copy logo: " + e.getMessage());
            }
        }
    }

    /**
     * Save shop identity
     */
    @FXML
    public void saveIdentity() {
        try {
            ShopIdentity identity = new ShopIdentity();
            identity.setPharmacyName(pharmacyNameField.getText());
            identity.setAddress(addressArea.getText());
            identity.setEmail(emailField.getText());
            identity.setLogoPath(logoPathField.getText());

            service.saveIdentity(identity, currentUser);
            showSuccess("Shop Identity Saved!");

        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
        }
    }

    /**
     * Navigate back to previous screen
     */
    @FXML
    public void goBack() {
        // TODO: Navigate back to main menu or previous screen
        System.out.println("Navigate back from Shop Identity Editor");
    }

    /**
     * Show success message dialog
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error message dialog
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
