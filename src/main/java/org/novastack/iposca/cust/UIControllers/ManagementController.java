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
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ManagementController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

    ObservableList<Customer> customers = FXCollections.observableArrayList();

    @FXML
    private TextField searchField;

    @FXML
    private TableColumn<Customer, String> address;

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
    private TableColumn<Customer, String> status;

    @FXML
    private Button backButton;

    @FXML
    private Button editButton;

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/cust/custMenu.fxml", "Customer Portal");
    }

    @FXML
    void editCustomer(MouseEvent event) throws IOException {
        if (customerTable.getSelectionModel().getSelectedItem() == null) {
            new CommonCalls().openErrorDialog("Please select a customer!");
            return;
        }

        Stage stage = (Stage) editButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/cust/custEdit.fxml"));
        Parent root = loader.load();

        EditController controller = loader.getController();
        controller.setID(customerTable.getSelectionModel().getSelectedItem().getCustomerID());

        stage.setTitle("Edit Customer: " + customerTable.getSelectionModel().getSelectedItem().getName());
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }

    @FXML
    void delCustomer(MouseEvent event) throws IOException {
        if (customerTable.getSelectionModel().getSelectedItem() == null) {
            new CommonCalls().openErrorDialog("Please select a customer!");
            return;
        }

        try {
            new Customer().deleteCustomer(customerTable.getSelectionModel().getSelectedItem().getCustomerID());
        } catch (DataAccessException e) {
            new CommonCalls().openErrorDialog(e.getMessage());
        }

        boolean ok = new CommonCalls().openConfirmationDialog("Are you sure you want to delete this customer?");
        if (!ok) {
            return;
        }

        refreshTable();
    }

    @FXML
    void setFixedDsc(MouseEvent event) throws IOException {
        Stage stage = (Stage) editButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/cust/fixedDsc.fxml", "Fixed Discount Plans");
    }

    @FXML
    void viewFlexiDsc(MouseEvent event) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/cust/flexiDsc.fxml"));
        Parent root = loader.load();
        stage.setTitle("View Flexible Discount Plans");
        stage.setScene(new javafx.scene.Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
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
