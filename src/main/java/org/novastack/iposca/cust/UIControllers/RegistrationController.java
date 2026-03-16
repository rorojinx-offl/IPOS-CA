package org.novastack.iposca.cust.UIControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.cust.plans.FixedDiscountPlan;
import org.novastack.iposca.cust.plans.FlexiDiscountPlan;
import org.novastack.iposca.utils.ui.CommonCalls;
import org.novastack.iposca.utils.ui.IValid;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegistrationController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        discountPlan.getItems().addAll(CustomerEnums.DiscountPlan.FIXED.name(), CustomerEnums.DiscountPlan.FLEXIBLE.name());
    }

    @FXML
    private TextField address;

    @FXML
    private Label addressWarning;

    @FXML
    private TextField credLimit;

    @FXML
    private Label credLimitWarning;

    @FXML
    private ChoiceBox<String> discountPlan;

    @FXML
    private Label discountPlanWarning;

    @FXML
    private TextField email;

    @FXML
    private Label emailWarning;

    @FXML
    private TextField name;

    @FXML
    private Label nameWarning;

    @FXML
    private TextField phone;

    @FXML
    private Label phoneWarning;

    @FXML
    private Button backButton;

    @FXML
    void registerUser(MouseEvent event) throws IOException {
        boolean allFieldsFilled = true;

        if (name.getText().isEmpty()) {
            nameWarning.setText("Name cannot be empty!");
            allFieldsFilled = false;
        }
        if (email.getText().isEmpty()) {
            emailWarning.setText("Email cannot be empty!");
            allFieldsFilled = false;
        }
        if (!IValid.checkEmail(email.getText()) && !email.getText().isEmpty()) {
            emailWarning.setText("Email Format is invalid!");
            allFieldsFilled = false;
        }
        if (address.getText().isEmpty()) {
            addressWarning.setText("Address cannot be empty!");
            allFieldsFilled = false;
        }
        if (phone.getText().isEmpty()) {
            phoneWarning.setText("Phone number cannot be empty!");
            allFieldsFilled = false;
        }
        if (!IValid.checkPhone(phone.getText()) && !phone.getText().isEmpty()) {
            phoneWarning.setText("Phone number format is invalid!");
            allFieldsFilled = false;
        }
        if (credLimit.getText().isEmpty()) {
            credLimitWarning.setText("Credit limit cannot be empty!");
            allFieldsFilled = false;
        }
        if (!IValid.checkCreditLimit(credLimit.getText()) && !credLimit.getText().isEmpty()) {
            credLimitWarning.setText("Credit limit value is invalid!");
            allFieldsFilled = false;
        }
        if (discountPlan.getSelectionModel().isEmpty()) {
            discountPlanWarning.setText("Discount plan cannot be empty!");
            allFieldsFilled = false;
        }

        if (allFieldsFilled) {
            Customer customer = new Customer(
                    name.getText(),
                    email.getText(),
                    address.getText(),
                    phone.getText(),
                    Float.parseFloat(credLimit.getText()),
                    discountPlan.getValue(),
                    CustomerEnums.AccountStatus.NORMAL.name()
            );

            try {
                customer.addCustomer(customer);
            } catch (DataAccessException e) {
                new CommonCalls().openErrorDialog(e.getMessage());
            }

            returnToParent(event);
            if (discountPlan.getValue().equals(CustomerEnums.DiscountPlan.FIXED.name())) {
                FixedDiscountPlan fdp = new FixedDiscountPlan(customer.getLatestID(), 10);
                fdp.addDiscount(fdp);
            } else {
                FlexiDiscountPlan fdp = new FlexiDiscountPlan(customer.getLatestID(), 0);
                fdp.addDiscount(fdp);
            }
        }
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/cust/custMenu.fxml");
    }
}
