package org.novastack.iposca.cust.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JRException;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerDebt;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.cust.customer.CustomerReminder;
import org.novastack.iposca.cust.reminders.ReminderFactory;
import org.novastack.iposca.cust.reminders.ReminderInfo;
import org.novastack.iposca.utils.common.TestReminderGen;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DebtController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        refreshTable();

        FilteredList<CustomerDebt> filteredData = new FilteredList<>(debtors, c -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(debtor -> {
                if (newVal == null || newVal.isEmpty()) {return true;}
                String filter = newVal.toLowerCase();
                return debtor.getCustomerName().toLowerCase().contains(filter);
            });
        });

        SortedList<CustomerDebt> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(customerTable.comparatorProperty());
        customerTable.setItems(sortedData);
    }

    ObservableList<CustomerDebt> debtors = FXCollections.observableArrayList();

    @FXML
    private Button backButton;

    @FXML
    private TextField searchField;


    @FXML
    private TableColumn<CustomerDebt, Float> balance;

    @FXML
    private TableColumn<CustomerDebt, Float> credLimit;

    @FXML
    private TableColumn<CustomerDebt, Integer> customerID;

    @FXML
    private TableView<CustomerDebt> customerTable;

    @FXML
    private TableColumn<CustomerDebt, String> name;

    @FXML
    private TableColumn<CustomerDebt, LocalDate> r1d;

    @FXML
    private TableColumn<CustomerDebt, String> r1s;

    @FXML
    private TableColumn<CustomerDebt, LocalDate> r2d;

    @FXML
    private TableColumn<CustomerDebt, String> r2s;

    @FXML
    private TableColumn<CustomerDebt, LocalDate> statca;

    @FXML
    private TableColumn<CustomerDebt, String> status;


    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/cust/custMenu.fxml", "Customer Portal");
    }

    @FXML
    void processRepayment(MouseEvent event) throws IOException {
        if (customerTable.getSelectionModel().getSelectedItem() == null) {
            new CommonCalls().openErrorDialog("Please select a customer!");
            return;
        }

        Stage stage = (Stage) backButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/cust/debtRepay.fxml"));
        Parent root = loader.load();

        RepaymentController controller = loader.getController();
        controller.receiver(customerTable.getSelectionModel().getSelectedItem().getBalance(), customerTable.getSelectionModel().getSelectedItem().getCustomerID());

        stage.setTitle("Customer");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }

    @FXML
    void generateReminders(MouseEvent event) throws IOException, JRException {
        CustomerDebt cd = customerTable.getSelectionModel().getSelectedItem();
        if (cd == null) {
            new CommonCalls().openErrorDialog("Please select a customer");
            return;
        }


        CustomerEnums.ReminderType type = null;
        if (cd.getStatus1Reminder() == null || cd.getStatus1Reminder().equals(CustomerEnums.ReminderStatus.NO_NEED.name())) {
            new CommonCalls().openErrorDialog("This customer does not need a reminder at this time!");
            return;
        }
        if (cd.getStatus1Reminder().equals(CustomerEnums.ReminderStatus.DUE.name()) && cd.getDate1Reminder() != null) {
            type = CustomerEnums.ReminderType.FIRST;
        }
        if (cd.getStatus1Reminder().equals(CustomerEnums.ReminderStatus.SENT.name()) && (cd.getStatus2Reminder() == null || cd.getStatus2Reminder().equals(CustomerEnums.ReminderStatus.NO_NEED.name()))) {
            new CommonCalls().openErrorDialog("This customer does not need a reminder at this time!");
            return;
        }
        if (cd.getStatus1Reminder().equals(CustomerEnums.ReminderStatus.SENT.name()) && cd.getStatus2Reminder().equals(CustomerEnums.ReminderStatus.SENT.name())) {
            new CommonCalls().openErrorDialog("All reminders for this month have already been sent to this customer!");
            return;
        }
        if (cd.getDate2Reminder() != null && cd.getStatus2Reminder().equals(CustomerEnums.ReminderStatus.DUE.name()) && cd.getStatus1Reminder().equals(CustomerEnums.ReminderStatus.SENT.name())) {
            type = CustomerEnums.ReminderType.SECOND;
        }

        if (type == null) {
            new CommonCalls().openErrorDialog("Error: Type is null!");
            return;
        }

        ReminderInfo info = ReminderInfo.setReminderInfo(cd, type);
        ReminderInfo.Merchant merchant = new ReminderInfo.Merchant(
                "T-Pharma",
                "123 Test Street, Test Town, Testshire, TE1 1ST",
                "test@tpharma.com",
                loadLogo());

        Path jrxml = Path.of("/jasper/cust/reminder.jrxml");
        int remNumber = type.equals(CustomerEnums.ReminderType.FIRST) ? 1 : 2;
        Path pdf = Path.of("generated-reports", "debt-reminder"+ remNumber +"-"+ cd.getCustomerID() +".pdf");
        ReminderFactory.generateReminder(info, merchant, type, pdf, jrxml);

        switch (type) {
            case FIRST -> {
                CustomerDebt.setReminderDateStatus(cd.getCustomerID(), CustomerEnums.ReminderType.FIRST, cd.getDate1Reminder().toString(), CustomerEnums.ReminderStatus.SENT.name());
                CustomerReminder cr = new CustomerReminder(cd.getCustomerID(),info.getIssueMonthYear().getMonth(),type,LocalDate.now());
                cr.recordReminder(cr);
                refreshTable();
            }
            case SECOND -> {
                CustomerDebt.setReminderDateStatus(cd.getCustomerID(), CustomerEnums.ReminderType.SECOND, cd.getDate2Reminder().toString(), CustomerEnums.ReminderStatus.SENT.name());
                CustomerReminder cr = new CustomerReminder(cd.getCustomerID(),info.getIssueMonthYear().getMonth(),type,LocalDate.now());
                cr.recordReminder(cr);
                refreshTable();
            }
            default -> new CommonCalls().openErrorDialog("Error: Type is null!");
        }
    }


    @FXML
    void manualAccountRestore(MouseEvent event) throws IOException {
        if (customerTable.getSelectionModel().getSelectedItem() == null || !customerTable.getSelectionModel().getSelectedItem().getCustStatus().equals(CustomerEnums.AccountStatus.IN_DEFAULT.name())) {
            new CommonCalls().openErrorDialog("Please select a customer whose account is in default!");
            return;
        }

        boolean ok = new CommonCalls().openConfirmationDialog("Are you sure you want to restore this customer back to normal?");
        if (!ok) {
            return;
        }

        Customer.updateAccountStatus(customerTable.getSelectionModel().getSelectedItem().getCustomerID(), CustomerEnums.AccountStatus.NORMAL.name());
        CustomerDebt.deleteDebt(customerTable.getSelectionModel().getSelectedItem().getCustomerID());
        refreshTable();
    }

    @FXML
    void viewReminders(MouseEvent event) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/cust/reminderHistory.fxml"));
        Parent root = loader.load();
        stage.setTitle("View Reminder Generation History");
        stage.setScene(new javafx.scene.Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public static byte[] loadLogo() throws IOException {
        try (InputStream in = TestReminderGen.class.getResourceAsStream("/ui/cust/assets/debt.png")) {
            if (in == null) {
                throw new IOException("Resource not found");
            }
            return in.readAllBytes();
        }
    }


    private void refreshTable() {
        customerID.setCellValueFactory(new PropertyValueFactory<CustomerDebt,Integer>("customerID"));
        name.setCellValueFactory(new PropertyValueFactory<CustomerDebt,String>("customerName"));
        credLimit.setCellValueFactory(new PropertyValueFactory<CustomerDebt,Float>("custCreditLimit"));
        status.setCellValueFactory(new PropertyValueFactory<CustomerDebt, String>("custStatus"));
        balance.setCellValueFactory(new PropertyValueFactory<CustomerDebt,Float>("balance"));
        r1d.setCellValueFactory(new PropertyValueFactory<CustomerDebt,LocalDate>("date1Reminder"));
        r1s.setCellValueFactory(new PropertyValueFactory<CustomerDebt,String>("status1Reminder"));
        r2d.setCellValueFactory(new PropertyValueFactory<CustomerDebt,LocalDate>("date2Reminder"));
        r2s.setCellValueFactory(new PropertyValueFactory<CustomerDebt,String>("status2Reminder"));
        statca.setCellValueFactory(new PropertyValueFactory<CustomerDebt, LocalDate>("statusChangedAt"));

        try {
            ArrayList<CustomerDebt> list = CustomerDebt.getAllDebts();
            debtors.setAll(list);
            customerTable.setItems(debtors);
        } catch (DataAccessException e) {
            try {
                new CommonCalls().openErrorDialog(e.getMessage());
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            Stage stage = (Stage) backButton.getScene().getWindow();
            try {
                new CommonCalls().traverse(stage, "/ui/cust/custMenu.fxml", "Customer Portal");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
