package org.novastack.iposca.rpt.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.novastack.iposca.rpt.factory.ReportFactory;
import org.novastack.iposca.rpt.model.TurnoverData;
import org.novastack.iposca.rpt.model.TurnoverSale;
import org.novastack.iposca.rpt.service.ReportService;
import org.novastack.iposca.session.Session;
import org.novastack.iposca.session.SessionManager;
import org.novastack.iposca.user.User;
import org.novastack.iposca.utils.ui.CommonCalls;
import org.novastack.iposca.utils.ui.ControllerTemplate;

import java.io.File;
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

//    @FXML
//    private Label totalOrdersLabel;

    @FXML
    private Label periodLabel;

    @FXML
    private Label generatedByLabel;

    @FXML
    private Label generatedTimestampLabel;

    @FXML
    private TableView<TurnoverSale> saleBreakdownTable;

    @FXML
    private TableColumn<TurnoverSale, Integer> saleIdColumn;

    @FXML
    private TableColumn<TurnoverSale, String> paymentMethodColumn;

    @FXML
    private TableColumn<TurnoverSale, String> saleDateColumn;

    @FXML
    private TableColumn<TurnoverSale, Float> amountColumn;

    @FXML
    private Button generateButton;

    @FXML
    private Button exportPdfButton;

    @FXML
    private Button backButton;

    private ReportService reportService;
    private TurnoverData currentData;
    private String currentUser;
    private ObservableList<TurnoverSale> saleBreakdown = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LocalDate now = LocalDate.now();
        startDatePicker.setValue(now.withDayOfMonth(1));
        endDatePicker.setValue(now);

        saleIdColumn.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        saleDateColumn.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Float amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : String.format("\u00A3%.2f", amount));
            }
        });
        saleBreakdownTable.setItems(saleBreakdown);

        currentUser = getCurrentUserDisplayName();
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
            File reportFile = ReportFactory.generateTurnoverReport(currentData, currentUser);
            try {
                ReportFactory.openReport(reportFile);
                new CommonCalls().openInfoDialog("Report exported successfully to " + reportFile.getPath());
            } catch (IOException openException) {
                new CommonCalls().openInfoDialog("Report exported successfully to " + reportFile.getPath()
                        + "\n\nThe PDF could not be opened automatically: " + openException.getMessage());
            }
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
//        totalOrdersLabel.setText(String.format("£%.2f", data.getTotalOrdersPlacedValue()));
        generatedByLabel.setText(data.getGeneratedBy());
        generatedTimestampLabel.setText(data.getGeneratedTimestamp().toString());
        saleBreakdown.setAll(data.getSales());

        if (data.getTotalSalesAmount() == 0 && data.getTotalSalesCount() == 0 && data.getTotalOrdersPlacedValue() == 0) {
            try {
                new CommonCalls().openErrorDialog("No Data Available for selected period.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getCurrentUserDisplayName() {
        Session session = SessionManager.getCurrentSession();
        User user = session == null ? null : session.getCurrentUser();
        if (user == null) {
            return "unknown";
        }

        String fullName = user.getFullName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName.trim();
        }

        return user.getUsername();
    }
}
