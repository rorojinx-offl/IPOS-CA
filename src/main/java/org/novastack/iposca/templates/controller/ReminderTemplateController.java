package org.novastack.iposca.templates.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.novastack.iposca.templates.model.TemplateModel;
import org.novastack.iposca.templates.service.TemplateService;
import org.novastack.iposca.templates.service.TemplateRenderer;

public class ReminderTemplateController {

    @FXML
    private ChoiceBox<String> reminderSelector;

    @FXML
    private TextArea bodyArea;

    @FXML
    private TextField footerField;

    private final TemplateService service = new TemplateService();

    /**
     * Initialize method - called after FXML is loaded
     */
    @FXML
    public void initialize() {
        // Populate the ChoiceBox
        reminderSelector.getItems().addAll("REMINDER_1", "REMINDER_2");
        reminderSelector.setValue("REMINDER_1");

        // Add listener to reload template when selection changes
        reminderSelector.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> loadTemplate(newVal)
        );

        // Load the initial template
        loadTemplate("REMINDER_1");
    }

    /**
     * Load the selected reminder template from database
     */
    private void loadTemplate(String type) {
        try {
            TemplateModel t = service.loadTemplate(type);
            if (t != null) {
                bodyArea.setText(t.body);
                footerField.setText(t.footer);
            }
        } catch (Exception e) {
            System.err.println("Failed to load template: " + e.getMessage());
        }
    }

    /**
     * Save the current reminder template
     */
    @FXML
    public void saveTemplate() {
        try {
            TemplateModel t = new TemplateModel();
            t.type = reminderSelector.getValue();
            t.body = bodyArea.getText();
            t.footer = footerField.getText();

            service.saveTemplate(t, "admin");
            showSuccess(t.type + " saved successfully!");

        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
        }
    }

    /**
     * Preview the current reminder template
     */
    @FXML
    public void previewTemplate() {
        TemplateModel t = new TemplateModel();
        t.type = reminderSelector.getValue();
        t.body = bodyArea.getText();
        t.footer = footerField.getText();

        String preview = TemplateRenderer.preview(t, "REMINDER");
        showPreview(preview);
    }

    /**
     * Reset the selected reminder template to default
     */
    @FXML
    public void resetToDefault() {
        try {
            String type = reminderSelector.getValue();
            boolean confirmed = showConfirmation(
                    "Reset " + type + " to default?\n\n" +
                            "This will overwrite your current template and cannot be undone."
            );
            if (confirmed) {
                service.resetToDefault(type, "admin");
                loadTemplate(type);
                showSuccess(type + " reset to default.");
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
        System.out.println("Navigate back from Reminder Template Editor");
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