package org.novastack.iposca.cust.UIControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.cust.Customer;
import org.novastack.iposca.cust.FixedDiscountPlan;
import org.novastack.iposca.cust.FlexiDiscountPlan;
import org.novastack.iposca.utils.ui.CommonCalls;
import org.novastack.iposca.utils.ui.IValid;

import java.io.IOException;

public class EditController implements Initializable {
    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle rb) {
        discountPlan.getItems().addAll(Customer.DiscountPlan.FIXED.name(), Customer.DiscountPlan.FLEXIBLE.name());
    }

    private int id;

    @FXML
    private TextField address;

    @FXML
    private Label addressWarning;

    @FXML
    private Button backButton;

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

    private String oldDS;

    public void setID(int ID) {
        this.id = ID;
        Customer customer = new Customer();
        customer = customer.getCustomer(id);
        name.setText(customer.getName());
        email.setText(customer.getEmail());
        address.setText(customer.getAddress());
        phone.setText(customer.getPhone());
        credLimit.setText(String.valueOf(customer.getCreditLimit()));
        discountPlan.getSelectionModel().select(customer.getDiscountPlan());
        oldDS = customer.getDiscountPlan();
    }

    @FXML
    void editUser(MouseEvent event) throws IOException {
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
                    id,
                    name.getText(),
                    email.getText(),
                    address.getText(),
                    phone.getText(),
                    Float.parseFloat(credLimit.getText()),
                    discountPlan.getValue(),
                    Customer.AccountStatus.NORMAL.name()
            );

            try {
                customer.editCustomer(customer);
            } catch (DataAccessException e) {
                new CommonCalls().openErrorDialog(e.getMessage());
            }

            if (!(checkDSChange(id)) && (discountPlan.getValue().equals(Customer.DiscountPlan.FLEXIBLE.name()))) {
                FixedDiscountPlan fdp = new FixedDiscountPlan();
                fdp.removeDiscount(id);

                FlexiDiscountPlan fdp2 = new FlexiDiscountPlan(id, 0);
                fdp2.addDiscount(fdp2);
            } else if (!(checkDSChange(id)) && (discountPlan.getValue().equals(Customer.DiscountPlan.FIXED.name()))) {
                FlexiDiscountPlan fdp = new FlexiDiscountPlan();
                fdp.removeDiscount(id);

                FixedDiscountPlan fdp2 = new FixedDiscountPlan(id, 20);
                fdp2.addDiscount(fdp2);
            }

            returnToParent(event);
        }
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/cust/custMgmt.fxml");

    }

    private boolean checkDSChange(int id) {
        String newDS = discountPlan.getValue();
        return newDS.equals(oldDS);
    }
}
