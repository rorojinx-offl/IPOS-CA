package org.novastack.iposca.sales.UIControllers;

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
import javafx.stage.Stage;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.sales.SaleService;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.util.ArrayList;

public class ASController implements Initializable {
    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle rb) {
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
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/sales/salesMenu.fxml", "Sales");
    }

    @FXML
    void selectItems(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/sales/selectItems.fxml"));
        Parent root = loader.load();

        SelectController controller = loader.getController();
        controller.receive(customerTable.getSelectionModel().getSelectedItem(), SaleService.CartMode.MEMBER);

        stage.setTitle("Select Items");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
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
                new CommonCalls().traverse(stage, "/ui/sales/salesMenu.fxml", "Sales");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
