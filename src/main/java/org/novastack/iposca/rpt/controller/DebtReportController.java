package org.novastack.iposca.rpt.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.novastack.iposca.rpt.factory.ReportFactory;
import org.novastack.iposca.rpt.model.DebtChangeData;
import org.novastack.iposca.rpt.service.ReportService;
import org.novastack.iposca.utils.ui.CommonCalls;
import org.novastack.iposca.utils.ui.ControllerTemplate;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;


public class DebtReportController extends ControllerTemplate {
    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Label openingDebtLabel;

    @FXML
    private Label paymentsReceivedLabel;

    @FXML
    private Label newDebtLabel;

    @FXML
    private Label closingDebtLabel;

    @FXML
    private Label formulaLabel;

    @FXML
    private Label totalDebtorsLabel;

    @FXML
    private Label totalPaymentsCountLabel;

    @FXML
    private Label totalCreditSalesLabel;

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
    private DebtChangeData currentData;
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
            currentData = reportService.getDebtChangeData(startDate, endDate);
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
            ReportFactory.generateDebtReport(currentData, currentUser);
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

    private void displayReport(DebtChangeData data) {
        periodLabel.setText(data.getStartDate() + " to " + data.getEndDate());
        openingDebtLabel.setText(String.format("£%.2f", data.getOpeningAggregateDebt()));
        paymentsReceivedLabel.setText(String.format("£%.2f", data.getPaymentsReceived()));
        newDebtLabel.setText(String.format("£%.2f", data.getNewDebtAccrued()));
        closingDebtLabel.setText(String.format("£%.2f", data.getClosingAggregateDebt()));
        formulaLabel.setText(String.format("£%.2f = £%.2f + £%.2f - £%.2f",
                data.getClosingAggregateDebt(),
                data.getOpeningAggregateDebt(),
                data.getNewDebtAccrued(),
                data.getPaymentsReceived()));
        totalDebtorsLabel.setText(String.valueOf(data.getTotalDebtorsCount()));
        totalPaymentsCountLabel.setText(String.valueOf(data.getTotalPaymentsCount()));
        totalCreditSalesLabel.setText(String.valueOf(data.getTotalCreditSalesCount()));
        generatedByLabel.setText(data.getGeneratedBy());
        generatedTimestampLabel.setText(data.getGeneratedTimestamp().toString());

        if (data.getOpeningAggregateDebt() == 0 && data.getPaymentsReceived() == 0 &&
                data.getNewDebtAccrued() == 0 && data.getClosingAggregateDebt() == 0) {
            try {
                new CommonCalls().openErrorDialog("No Data Available for selected period.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
