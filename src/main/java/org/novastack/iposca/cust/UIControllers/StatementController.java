package org.novastack.iposca.cust.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.statement.StatementFactory;
import org.novastack.iposca.cust.statement.StatementService;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class StatementController implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshTable();

        FilteredList<Customer> filteredData = new FilteredList<>(customers, c -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(customer -> {
                if (newVal == null || newVal.isEmpty()) {return true;}
                String filter = newVal.toLowerCase();
                return customer.getName().toLowerCase().contains(filter);
            });
        });

        SortedList<Customer> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(customerTable.comparatorProperty());
        customerTable.setItems(sortedData);
    }

    @FXML
    private TableColumn<Customer, String> address;

    @FXML
    private Button backButton;

    @FXML
    private TableColumn<Customer, Float> credLimit;

    @FXML
    private TableColumn<Customer, Integer> customerID;

    @FXML
    private TableView<Customer> customerTable;

    @FXML
    private TableColumn<Customer, String> discountPlan;

    @FXML
    private TableColumn<Customer, String> email;

    @FXML
    private TableColumn<Customer, String> name;

    @FXML
    private TableColumn<Customer, String> phone;

    @FXML
    private TextField searchField;

    @FXML
    private TableColumn<Customer, String> status;

    ObservableList<Customer> customers = FXCollections.observableArrayList();

    @FXML
    void generateStatement(MouseEvent event) throws IOException {
        Customer cust = customerTable.getSelectionModel().getSelectedItem();
        if (cust == null) {
            new CommonCalls().openErrorDialog("Please select a customer!");
            return;
        }

        StatementService.StatementInfo info = StatementService.buildStatementData(cust.getCustomerID(), YearMonth.now());
        if (info == null) {
            new CommonCalls().openErrorDialog("The customer may have not made a purchase in the previous billing month!");
            return;
        }

        try {
            StatementFactory.generateStatement(info, YearMonth.now());
        } catch (Exception e) {
            new CommonCalls().openErrorDialog(e.getMessage());
        }
    }

    @FXML
    void returnToParent(MouseEvent event) {
        Stage stage = (Stage) backButton.getScene().getWindow();
        try {
            new CommonCalls().traverse(stage, "/ui/cust/custMenu.fxml", "Customer Portal");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void refreshTable() {
        customerID.setCellValueFactory(new PropertyValueFactory<Customer,Integer>("customerID"));
        name.setCellValueFactory(new PropertyValueFactory<Customer,String>("name"));
        email.setCellValueFactory(new PropertyValueFactory<Customer,String>("email"));
        address.setCellValueFactory(new PropertyValueFactory<Customer,String>("address"));
        phone.setCellValueFactory(new PropertyValueFactory<Customer,String>("phone"));
        credLimit.setCellValueFactory(new PropertyValueFactory<Customer,Float>("creditLimit"));
        discountPlan.setCellValueFactory(new PropertyValueFactory<Customer,String>("discountPlan"));
        status.setCellValueFactory(new PropertyValueFactory<Customer,String>("status"));

        try {
            ArrayList<Customer> list = StatementService.getEligibleCustomers();
            customers.setAll(list);
            customerTable.setItems(customers);
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
