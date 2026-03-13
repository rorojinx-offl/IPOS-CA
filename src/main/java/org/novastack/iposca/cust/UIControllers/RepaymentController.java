package org.novastack.iposca.cust.UIControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.cust.customer.CustomerPayment;
import org.novastack.iposca.utils.ui.CommonCalls;
import org.novastack.iposca.utils.ui.IValid;

import java.io.IOException;
import java.time.LocalDate;

public class RepaymentController implements Initializable {
    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle rb) {
        methodChoice.getItems().addAll(CustomerEnums.PaymentMethod.CASH.name(), CustomerEnums.PaymentMethod.CARD.name());
        methodChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            updateDynamicFields(newValue);
        });

        cardChoice.getItems().addAll(CustomerEnums.CardType.VISA.name(), CustomerEnums.CardType.MASTERCARD.name(), CustomerEnums.CardType.AMEX.name(), CustomerEnums.CardType.OTHER.name());
    }

    private float realBalance;
    private int customerID;

    @FXML
    private TextField amount;

    @FXML
    private Label amountWarning;

    @FXML
    private Button backButton;

    @FXML
    private ChoiceBox<String> cardChoice;

    @FXML
    private DatePicker cardExp;

    @FXML
    private Label cardExpWarning;

    @FXML
    private TextField cardNo;

    @FXML
    private Label cardNoWarning;

    @FXML
    private Label cardWarning;

    @FXML
    private ChoiceBox<String> methodChoice;

    @FXML
    private Label methodWarning;

    @FXML
    private HBox cardExpCon;

    @FXML
    private HBox cardNoCon;

    @FXML
    private HBox cardTypeCon;

    public void receiver(float amount, int custID) {
        customerID = custID;
        realBalance = amount;
        this.amount.setText(String.valueOf(amount));
    }

    @FXML
    void absolveDebt(MouseEvent event) throws IOException {
        boolean allFieldsFilled = true;

        if (amount.getText().isEmpty()) {
            amountWarning.setText("Amount cannot be empty!");
            allFieldsFilled = false;
        }

        if (IValid.checkCreditLimit(amount.getText()) && !amount.getText().isEmpty()) {
            amountWarning.setText("Amount format is invalid!");
        }

        if (methodChoice.getSelectionModel().isEmpty() || methodChoice.getSelectionModel().getSelectedItem() == null) {
            methodWarning.setText("Payment method cannot be empty!");
            allFieldsFilled = false;
        }

        if (methodChoice.getSelectionModel() != null || !methodChoice.getSelectionModel().isEmpty()) {
            if (methodChoice.getSelectionModel().getSelectedItem().equals(CustomerEnums.PaymentMethod.CARD.name())) {
                if (cardChoice.getSelectionModel().isEmpty()) {
                    cardWarning.setText("Card type cannot be empty!");
                    allFieldsFilled = false;
                }
                if (cardNo.getText().isEmpty()) {
                    cardNoWarning.setText("Card number cannot be empty!");
                    allFieldsFilled = false;
                }
                if (cardExp.getValue() == null) {
                    cardExpWarning.setText("Card expiration date cannot be empty!");
                    allFieldsFilled = false;
                }
            }
        }

        if (Float.parseFloat(amount.getText()) != realBalance) {
            amountWarning.setText("Amount is not equal to the balance!");
            allFieldsFilled = false;
        }

        if (allFieldsFilled) {
            CustomerPayment crp;

            if (methodChoice.getSelectionModel().getSelectedItem().equals(CustomerEnums.PaymentMethod.CASH.name())) {
                crp = new CustomerPayment(customerID, Float.parseFloat(amount.getText()), LocalDate.now(), CustomerEnums.PaymentMethod.CASH.name());
            } else {
                String first4 = cardNo.getText().substring(0, 4);
                String last4 = cardNo.getText().substring(cardNo.getText().length() - 4);
                crp = new CustomerPayment(customerID, Float.parseFloat(amount.getText()), LocalDate.now(), CustomerEnums.PaymentMethod.CARD.name(), cardChoice.getSelectionModel().getSelectedItem(), first4, last4, cardExp.getValue().toString());
            }

            crp.addRepayment(crp);

            Customer cus = new Customer().getCustomer(customerID);
            if (cus.getStatus().equals(Customer.AccountStatus.SUSPENDED.name())) {
                Customer.updateAccountStatus(customerID, Customer.AccountStatus.NORMAL.name());
            }

            returnToParent(event);
        }
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/cust/debtMgmt.fxml");
    }

    private void updateDynamicFields(String selection) {
        if (selection.equals(CustomerEnums.PaymentMethod.CARD.name())) {
            cardTypeCon.setVisible(true);
            cardNoCon.setVisible(true);
            cardExpCon.setVisible(true);
        } else {
            cardTypeCon.setVisible(false);
            cardNoCon.setVisible(false);
            cardExpCon.setVisible(false);
        }
    }


}
