package org.novastack.iposca.cust.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.cust.Customer;
import org.novastack.iposca.cust.customer.CustomerDebt;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DebtController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        refreshTable();
    }

    ObservableList<CustomerDebt> debtors = FXCollections.observableArrayList();

    @FXML
    private Button backButton;

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
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/cust/custMenu.fxml");
    }

    private void refreshTable() {
        customerID.setCellValueFactory(new PropertyValueFactory<CustomerDebt,Integer>("customerID"));
        name.setCellValueFactory(new PropertyValueFactory<CustomerDebt,String>("customerName"));
        credLimit.setCellValueFactory(new PropertyValueFactory<CustomerDebt,Float>("custCreditLimit"));
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
                new CommonCalls().traverse(stage, "/ui/cust/custMenu.fxml");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
