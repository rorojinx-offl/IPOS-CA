package org.novastack.iposca.cust.UIControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.novastack.iposca.cust.customer.CustomerEnums;

public class RepaymentController implements Initializable {
    @Override
    public void initialize(java.net.URL url, java.util.ResourceBundle rb) {
        methodChoice.getItems().addAll(CustomerEnums.PaymentMethod.CASH.name(), CustomerEnums.PaymentMethod.CARD.name());
        methodChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            updateDyanmicFields(newValue);
        });

        cardChoice.getItems().addAll(CustomerEnums.CardType.VISA.name(), CustomerEnums.CardType.MASTERCARD.name(), CustomerEnums.CardType.AMEX.name(), CustomerEnums.CardType.OTHER.name());
    }


    @FXML
    private TextField amount;

    @FXML
    private Label amountWarning;

    @FXML
    private Button backButton;

    @FXML
    private ChoiceBox<String> cardChoice;

    @FXML
    private TextField cardExp;

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


    @FXML
    void absolveDebt(MouseEvent event) {

    }

    @FXML
    void returnToParent(MouseEvent event) {

    }

    private void updateDyanmicFields(String selection) {
        if (selection.equals(CustomerEnums.PaymentMethod.CARD.name())) {
            cardTypeCon.setVisible(true);
            cardNoCon.setVisible(true);
            cardExpCon.setVisible(true);
        }
    }


}
