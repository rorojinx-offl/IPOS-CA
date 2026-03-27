package org.novastack.iposca.templates.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.novastack.iposca.templates.model.TemplateModel;
import org.novastack.iposca.templates.service.TemplateService;
import org.novastack.iposca.templates.service.TemplateRenderer;

public class ReceiptTemplateController {

    @FXML
    private TextField headerField;

    @FXML
    private TextArea bodyArea;

    @FXML
    private TextField footerField;

    @FXML
    private CheckBox logoCheck;

    private final TemplateService service = new TemplateService();

    /**
     * Initialize method - called after FXML is loaded
     */
    @FXML
    public void initialize() {
        loadExistingTemplate();
    }

    /**
     * Load the existing receipt template from database
     */
    private void loadExistingTemplate() {
        try {
            TemplateModel t = service.loadTemplate("RECEIPT");
            if (t != null) {
                headerField.setText(t.header);
                bodyArea.setText(t.body);
                footerField.setText(t.footer);
                logoCheck.setSelected(t.showLogo);
            }
        } catch (Exception e) {
            System.err.println("Failed to load template: " + e.getMessage());
        }
    }

    /**
     * Save the receipt template
     */
    @FXML
    public void saveTemplate() {
        try {
            TemplateModel t = new TemplateModel();
            t.type = "RECEIPT";
            t.header = headerField.getText();
            t.body = bodyArea.getText();
            t.footer = footerField.getText();
            t.showLogo = logoCheck.isSelected();

            service.saveTemplate(t, "admin");
            showSuccess("Receipt template saved successfully!");

        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
        }
    }

    /**
     * Preview the receipt template
     */
    @FXML
    public void previewTemplate() {
        TemplateModel t = new TemplateModel();
        t.type = "RECEIPT";
        t.header = headerField.getText();
        t.body = bodyArea.getText();
        t.footer = footerField.getText();
        t.showLogo = logoCheck.isSelected();

        String preview = TemplateRenderer.preview(t, "RECEIPT");
        showPreview(preview);
    }

    /**
     * Reset receipt template to default
     */
    @FXML
    public void resetToDefault() {
        try {
            boolean confirmed = showConfirmation(
                    "Reset receipt template to default?\n\n" +
                            "This will overwrite your current template and cannot be undone."
            );
            if (confirmed) {
                service.resetToDefault("RECEIPT", "admin");
                loadExistingTemplate();
                showSuccess("Receipt template reset to default.");
            }
        } catch (Exception e) {
            showError("Failed to reset: " + e.getMessage());
        }
    }

    /**
     * Navigate back to previous screen
     */
    @FXML
    public void goBack() {
        // TODO: Navigate back to main menu or previous screen
        System.out.println("Navigate back from Receipt Template Editor");
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

    /**
     * Show preview dialog
     */
    private void showPreview(String preview) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Preview");
        alert.setHeaderText("Template Preview");
        alert.setContentText(preview);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }

    /**
     * Show confirmation dialog and return user choice
     */
    private boolean showConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}