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
import org.novastack.iposca.cust.Customer;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegistrationController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        discountPlan.getItems().addAll(Customer.DiscountPlan.FIXED.name(), Customer.DiscountPlan.FLEXIBLE.name());
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
        if (!checkEmail(email.getText())) {
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
        if (!checkPhone(phone.getText())) {
            phoneWarning.setText("Phone number format is invalid!");
            allFieldsFilled = false;
        }
        if (credLimit.getText().isEmpty()) {
            credLimitWarning.setText("Credit limit cannot be empty!");
            allFieldsFilled = false;
        }
        if (!checkCreditLimit(credLimit.getText())) {
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
                    credLimit.getLength(),
                    discountPlan.getValue(),
                    Customer.AccountStatus.NORMAL.name()
            );

            try {
                customer.addCustomer(customer);
            } catch (DataAccessException e) {
                new CommonCalls().openErrorDialog(e.getMessage());
            }
            returnToParent(event);
        }
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/cust/custMenu.fxml");
    }



    private boolean checkCreditLimit(String creditLimit) {
        float limit;
        try {
            limit = Float.parseFloat(creditLimit);
        } catch (NumberFormatException e) {
            return false;
        }
        return limit > 0;
    }

    private boolean checkEmail(String email) {
        return email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    }

    private boolean checkPhone(String phone) {
        return phone.matches("^\\d{11}$");
    }
}
