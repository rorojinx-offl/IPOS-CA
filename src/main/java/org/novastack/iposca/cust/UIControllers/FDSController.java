package org.novastack.iposca.cust.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.cust.DiscountPlans;
import org.novastack.iposca.cust.FixedDiscountPlan;
import org.novastack.iposca.utils.ui.CommonCalls;
import org.novastack.iposca.utils.ui.IValid;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class FDSController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        refreshTable();
        rateSpinner.setValueFactory(rateValueFactory);
    }

    ObservableList<DiscountPlans> planHolders = FXCollections.observableArrayList();

    @FXML
    private TableColumn<FixedDiscountPlan, Integer> customerID;

    @FXML
    private TableView<DiscountPlans> customerTable;

    @FXML
    private TableColumn<FixedDiscountPlan, String> name;

    @FXML
    private TableColumn<FixedDiscountPlan, Integer> rate;

    @FXML
    private TextField newRate;

    @FXML
    private Spinner<Integer> rateSpinner;

    SpinnerValueFactory<Integer> rateValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);

    @FXML
    void selectRate(MouseEvent event) throws IOException {
        if (customerTable.getSelectionModel().getSelectedItem() == null) {
            new CommonCalls().openErrorDialog("Please select a customer!");
            return;
        }

        int oldDiscountRate = new FixedDiscountPlan().getCurrentDiscountRate(customerTable.getSelectionModel().getSelectedItem().getCustomerID());

        if (!IValid.checkRate(newRate.getText()) || newRate.getText().isEmpty()) {
            new CommonCalls().openErrorDialog("The value is either invalid or empty!");
            return;
        }

        int nr = Integer.parseInt(newRate.getText());
        if (oldDiscountRate == nr) {
            return;
        }

        FixedDiscountPlan fdp = new FixedDiscountPlan(customerTable.getSelectionModel().getSelectedItem().getCustomerID(), nr);
        fdp.modifyRate(fdp);
        customerTable.getItems().removeAll(customerTable.getSelectionModel().getTableView().getItems());
        refreshTable();
    }

    @FXML
    void allRate(MouseEvent event) throws IOException {
        if (!IValid.checkRate(rateSpinner.getValue().toString()) || rateSpinner.getValue().toString().isEmpty()) {
            new CommonCalls().openErrorDialog("The value is either invalid or empty!");
            return;
        }

        int nr = rateSpinner.getValue();
        FixedDiscountPlan.modifyRateForAll(nr);
        customerTable.getItems().removeAll(customerTable.getSelectionModel().getTableView().getItems());
        refreshTable();
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) customerTable.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/cust/custMgmt.fxml");
    }

    private void refreshTable() {
        customerID.setCellValueFactory(new PropertyValueFactory<FixedDiscountPlan, Integer>("customerID"));
        name.setCellValueFactory(new PropertyValueFactory<FixedDiscountPlan, String>("customerName"));
        rate.setCellValueFactory(new PropertyValueFactory<FixedDiscountPlan, Integer>("discountRate"));

        FixedDiscountPlan plan = new FixedDiscountPlan();
        try {
            ArrayList<DiscountPlans> list = plan.getAllDiscounts();
            planHolders.addAll(list);
            customerTable.setItems(planHolders);
            customerTable.setEditable(true);
        } catch (DataAccessException e) {
            try {
                new CommonCalls().openErrorDialog(e.getMessage());
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            Stage stage = (Stage) customerTable.getScene().getWindow();
            try {
                new CommonCalls().traverse(stage, "/ui/cust/custMenu.fxml");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
