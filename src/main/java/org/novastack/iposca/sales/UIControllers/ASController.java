package org.novastack.iposca.sales.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import org.novastack.iposca.cust.UIControllers.ManagementController;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.util.ArrayList;

public class ASController implements Initializable {
    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle rb) {
        refreshTable();
    }

    ObservableList<Customer> customers = FXCollections.observableArrayList();

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

    @FXML
    void returnToParent(MouseEvent event) {

    }

    @FXML
    void selectItems(MouseEvent event) {

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

        Customer customer = new Customer();
        try {
            ArrayList<Customer> list = customer.getAllCustomers();
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
