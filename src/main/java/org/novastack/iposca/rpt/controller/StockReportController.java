package org.novastack.iposca.rpt.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.novastack.iposca.rpt.factory.ReportFactory;
import org.novastack.iposca.rpt.model.StockItem;
import org.novastack.iposca.rpt.service.ReportService;
import org.novastack.iposca.session.Session;
import org.novastack.iposca.session.SessionManager;
import org.novastack.iposca.user.User;
import org.novastack.iposca.utils.ui.CommonCalls;
import org.novastack.iposca.utils.ui.ControllerTemplate;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class StockReportController extends ControllerTemplate {
    @FXML
    private ComboBox<String> stockStatusCombo;

    @FXML
    private TextField searchField;

    @FXML
    private Button generateButton;

    @FXML
    private Button exportPdfButton;

    @FXML
    private Button backButton;

    @FXML
    private TableView<StockItem> stockTable;

    @FXML
    private TableColumn<StockItem, Integer> idColumn;

    @FXML
    private TableColumn<StockItem, String> nameColumn;

    @FXML
    private TableColumn<StockItem, Integer> quantityColumn;

    @FXML
    private TableColumn<StockItem, Integer> thresholdColumn;

    @FXML
    private TableColumn<StockItem, Boolean> lowStockColumn;

    @FXML
    private TableColumn<StockItem, Float> bulkCostColumn;

    @FXML
    private TableColumn<StockItem, Float> retailPriceColumn;

    @FXML
    private TableColumn<StockItem, Float> vatRateColumn;

    @FXML
    private TableColumn<StockItem, Float> vatAmountColumn;

    @FXML
    private TableColumn<StockItem, Float> totalValueColumn;

    @FXML
    private Label totalItemsLabel;

    @FXML
    private Label totalUnitsLabel;

    @FXML
    private Label totalStockValueLabel;

    @FXML
    private Label totalVatLabel;

    private ObservableList<StockItem> stockItems = FXCollections.observableArrayList();
    private FilteredList<StockItem> filteredStockItems;
    private ReportService reportService;
    private String currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        stockStatusCombo.getItems().addAll("ALL", "LOW_STOCK", "OUT_OF_STOCK");
        stockStatusCombo.setValue("ALL");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        thresholdColumn.setCellValueFactory(new PropertyValueFactory<>("reorderThreshold"));
        lowStockColumn.setCellValueFactory(new PropertyValueFactory<>("lowStock"));
        bulkCostColumn.setCellValueFactory(new PropertyValueFactory<>("bulkCost"));
        retailPriceColumn.setCellValueFactory(new PropertyValueFactory<>("retailPrice"));
        vatRateColumn.setCellValueFactory(new PropertyValueFactory<>("vatRate"));
        vatAmountColumn.setCellValueFactory(new PropertyValueFactory<>("vatAmount"));
        totalValueColumn.setCellValueFactory(new PropertyValueFactory<>("totalStockValue"));

        currentUser = getCurrentUserDisplayName();
        reportService = new ReportService(currentUser);
        filteredStockItems = new FilteredList<>(stockItems, item -> true);
        SortedList<StockItem> sortedData = new SortedList<>(filteredStockItems);
        sortedData.comparatorProperty().bind(stockTable.comparatorProperty());
        stockTable.setItems(sortedData);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applySearchFilter();
            updateTotals();
            exportPdfButton.setDisable(filteredStockItems.isEmpty());
        });
        exportPdfButton.setDisable(true);
    }

    @FXML
    void generateReport(MouseEvent event) {
        String status = stockStatusCombo.getValue();
        try {
            List<StockItem> items = reportService.getStockData(status, "");
            stockItems.setAll(items);
            applySearchFilter();

            updateTotals();
            exportPdfButton.setDisable(filteredStockItems.isEmpty());

            if (filteredStockItems.isEmpty()) {
                new CommonCalls().openErrorDialog("No data found for selected filters.");
            }
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
        List<StockItem> displayedItems = getDisplayedStockItems();
        if (displayedItems.isEmpty()) {
            try {
                new CommonCalls().openErrorDialog("No data to export. Please generate report first.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            ReportFactory.generateStockReport(displayedItems, currentUser);
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

    private void updateTotals() {
        List<StockItem> displayedItems = getDisplayedStockItems();
        int totalItems = displayedItems.size();
        int totalUnits = displayedItems.stream().mapToInt(StockItem::getQuantity).sum();
        float totalStockValue = (float) displayedItems.stream().mapToDouble(StockItem::getTotalStockValue).sum();
        float totalVat = (float) displayedItems.stream().mapToDouble(StockItem::getVatAmount).sum();

        totalItemsLabel.setText(String.valueOf(totalItems));
        totalUnitsLabel.setText(String.valueOf(totalUnits));
        totalStockValueLabel.setText(String.format("£%.2f", totalStockValue));
        totalVatLabel.setText(String.format("£%.2f", totalVat));
    }

    private void applySearchFilter() {
        String searchText = searchField.getText();
        String search = searchText == null ? "" : searchText.trim().toLowerCase();

        filteredStockItems.setPredicate(item -> search.isEmpty()
                || String.valueOf(item.getId()).contains(search)
                || item.getName().toLowerCase().contains(search));
    }

    private List<StockItem> getDisplayedStockItems() {
        if (filteredStockItems == null) {
            return new ArrayList<>(stockItems);
        }

        return new ArrayList<>(filteredStockItems);
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
