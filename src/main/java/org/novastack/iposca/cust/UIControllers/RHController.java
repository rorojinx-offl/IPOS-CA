package org.novastack.iposca.cust.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.cust.customer.CustomerReminder;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class RHController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        id.setCellValueFactory(new PropertyValueFactory<CustomerReminder, Integer>("custID"));
        name.setCellValueFactory(new PropertyValueFactory<CustomerReminder, String>("customerName"));
        billingMonth.setCellValueFactory(new PropertyValueFactory<CustomerReminder, Month>("billingMonth"));
        type.setCellValueFactory(new PropertyValueFactory<CustomerReminder, CustomerEnums.ReminderType>("reminderType"));
        date.setCellValueFactory(new PropertyValueFactory<CustomerReminder, LocalDate>("reminderDate"));

        try {
            ArrayList<CustomerReminder> list = CustomerReminder.getAllReminders();
            reminders.addAll(list);
            remTable.setItems(reminders);
            remTable.setEditable(false);
        } catch (DataAccessException e) {
            try {
                new CommonCalls().openErrorDialog(e.getMessage());
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            Stage stage = (Stage) remTable.getScene().getWindow();
            try {
                new CommonCalls().traverse(stage, "/ui/cust/custMenu.fxml", "Customer Portal");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private final ObservableList<CustomerReminder> reminders = FXCollections.observableArrayList();

    @FXML
    private TableColumn<CustomerReminder, Month> billingMonth;
    @FXML
    private TableColumn<CustomerReminder, LocalDate> date;

    @FXML
    private TableColumn<CustomerReminder, Integer> id;

    @FXML
    private TableColumn<CustomerReminder, String> name;

    @FXML
    private TableView<CustomerReminder> remTable;

    @FXML
    private TableColumn<CustomerReminder, CustomerEnums.ReminderType> type;
}
