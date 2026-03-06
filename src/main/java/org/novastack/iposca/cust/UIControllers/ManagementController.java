package org.novastack.iposca.cust.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.cust.Customer;
import org.novastack.iposca.utils.ui.CommonCalls;
import org.novastack.iposca.utils.ui.ErrorController;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ManagementController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
            customers.addAll(list);
            customerTable.setItems(customers);
        } catch (DataAccessException e) {
            try {
                new CommonCalls().openErrorDialog(e.getMessage());
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            Stage stage = (Stage) backButton.getScene().getWindow();
            try {
                new CommonCalls().traverse(stage, "/ui/cust/custMenu.fxml");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    ObservableList<Customer> customers = FXCollections.observableArrayList();

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
        new CommonCalls().traverse(stage, "/ui/cust/custMenu.fxml");
    }

    @FXML
    void editCustomer(MouseEvent event) throws IOException {
        Stage stage = (Stage) editButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/cust/custEdit.fxml"));
        Parent root = loader.load();

        EditController controller = loader.getController();
        controller.setID(customerTable.getSelectionModel().getSelectedItem().getCustomerID());

        stage.setTitle("Customer");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }
}
