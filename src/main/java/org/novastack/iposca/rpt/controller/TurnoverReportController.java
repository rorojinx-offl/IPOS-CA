package org.novastack.iposca.rpt.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.novastack.iposca.rpt.factory.ReportFactory;
import org.novastack.iposca.rpt.model.TurnoverData;
import org.novastack.iposca.rpt.service.ReportService;
import org.novastack.iposca.utils.ui.CommonCalls;
import org.novastack.iposca.utils.ui.ControllerTemplate;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class TurnoverReportController extends ControllerTemplate {
    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Label totalSalesAmountLabel;

    @FXML
    private Label totalSalesCountLabel;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label periodLabel;

    @FXML
    private Label generatedByLabel;

    @FXML
    private Label generatedTimestampLabel;

    @FXML
    private Button generateButton;

    @FXML
    private Button exportPdfButton;

    @FXML
    private Button backButton;

    private ReportService reportService;
    private TurnoverData currentData;
    private String currentUser = "admin";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LocalDate now = LocalDate.now();
        startDatePicker.setValue(now.withDayOfMonth(1));
        endDatePicker.setValue(now);

        reportService = new ReportService(currentUser);
        exportPdfButton.setDisable(true);
    }

    @FXML
    void generateReport(MouseEvent event) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            try {
                new CommonCalls().openErrorDialog("Please select both start and end dates.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        if (startDate.isAfter(endDate)) {
            try {
                new CommonCalls().openErrorDialog("Start date cannot be after end date.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            currentData = reportService.getTurnoverData(startDate, endDate);
            displayReport(currentData);
            exportPdfButton.setDisable(false);
        } catch (Exception e) {
            try {
                new CommonCalls().openErrorDialog("Failed to generate report: " + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    void exportToPdf(MouseEvent event) {
        if (currentData == null) {
            try {
                new CommonCalls().openErrorDialog("No report data to export. Please generate report first.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            ReportFactory.generateTurnoverReport(currentData, currentUser);
            new CommonCalls().openErrorDialog("Report exported successfully to generated-reports/");
        } catch (Exception e) {
            try {
                new CommonCalls().openErrorDialog("Failed to export PDF: " + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    void goBack(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/rpt/reportsMenu.fxml", "Reports");
    }

    private void displayReport(TurnoverData data) {
        periodLabel.setText(data.getReportPeriodStart() + " to " + data.getReportPeriodEnd());
        totalSalesAmountLabel.setText(String.format("£%.2f", data.getTotalSalesAmount()));
        totalSalesCountLabel.setText(String.valueOf(data.getTotalSalesCount()));
        totalOrdersLabel.setText(String.format("£%.2f", data.getTotalOrdersPlacedValue()));
        generatedByLabel.setText(data.getGeneratedBy());
        generatedTimestampLabel.setText(data.getGeneratedTimestamp().toString());

        if (data.getTotalSalesAmount() == 0 && data.getTotalSalesCount() == 0 && data.getTotalOrdersPlacedValue() == 0) {
            try {
                new CommonCalls().openErrorDialog("No Data Available for selected period.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
