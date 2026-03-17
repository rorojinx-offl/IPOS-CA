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
import org.novastack.iposca.cust.plans.DiscountPlans;
import org.novastack.iposca.cust.plans.FlexiDiscountPlan;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class FlexiDSContoller implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        id.setCellValueFactory(new PropertyValueFactory<FlexiDiscountPlan, Integer>("customerID"));
        name.setCellValueFactory(new PropertyValueFactory<FlexiDiscountPlan, String>("customerName"));
        rate.setCellValueFactory(new PropertyValueFactory<FlexiDiscountPlan, Integer>("discountRate"));
        spending.setCellValueFactory(new PropertyValueFactory<FlexiDiscountPlan, Float>("custSpending"));

        FlexiDiscountPlan plan = new FlexiDiscountPlan();
        try {
            ArrayList<DiscountPlans> list = plan.getAllDiscounts();
            planHolders.addAll(list);
            flexiTable.setItems(planHolders);
            flexiTable.setEditable(true);
        } catch (DataAccessException e) {
            try {
                new CommonCalls().openErrorDialog(e.getMessage());
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            Stage stage = (Stage) flexiTable.getScene().getWindow();
            try {
                new CommonCalls().traverse(stage, "/ui/cust/custMenu.fxml", "Customer Portal");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    private ObservableList<DiscountPlans> planHolders = FXCollections.observableArrayList();

    @FXML
    private TableColumn<FlexiDiscountPlan, Integer> id;

    @FXML
    private TableColumn<FlexiDiscountPlan, String> name;

    @FXML
    private TableColumn<FlexiDiscountPlan, Integer> rate;

    @FXML
    private TableColumn<FlexiDiscountPlan, Float> spending;

    @FXML
    private TableView<DiscountPlans> flexiTable;
}
