package org.novastack.iposca.sales.UIControllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.novastack.iposca.cust.UIControllers.EditController;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.sales.SaleService;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ResourceBundle;

public class CardController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        vendor.getItems().addAll(CustomerEnums.CardType.VISA.name(), CustomerEnums.CardType.MASTERCARD.name(), CustomerEnums.CardType.AMEX.name(), CustomerEnums.CardType.OTHER.name());
    }

    private Customer customer;
    private SaleService.SaleDraft draft;
    private ObservableList<SaleLine> cartSession;


    public void receive(Customer cust, SaleService.SaleDraft sd, ObservableList<SaleLine> cs) {
        customer = cust;
        draft = sd;
        cartSession = cs;
    }

    public SaleService.Callback checkCallback() {
        if (customer != null && cartSession != null) {
            return new SaleService.Callback(customer, cartSession);
        } else {
            return null;
        }
    }

    @FXML
    private Button backButton;

    @FXML
    private DatePicker cardExp;

    @FXML
    private Label cardExpWarning;

    @FXML
    private TextField cardNo;

    @FXML
    private Label cardNoWarning;

    @FXML
    private ChoiceBox<String> vendor;

    @FXML
    private Label vendorWarning;

    @FXML
    void pay(MouseEvent event) throws IOException {
        boolean allFieldsFilled = true;

        if (vendor.getValue() == null) {
            vendorWarning.setText("Please select a vendor!");
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

        if (allFieldsFilled) {
            boolean ok = new CommonCalls().openConfirmationDialog("Proceed with payment?");
            if (!ok) {
                return;
            }

            if (customer == null || draft == null) {
                new CommonCalls().openErrorDialog("Unable to parse customer or order!");
                Stage stage = (Stage) backButton.getScene().getWindow();
                new CommonCalls().traverse(stage, "/ui/sales/salesAccount.fxml", "Sale for Account Holder");
                return;
            }

            if (!customer.getStatus().equals(CustomerEnums.AccountStatus.NORMAL.name())) {
                new CommonCalls().openErrorDialog("Customer is not authorised to make payments!");
                return;
            }

            String first4 = cardNo.getText().substring(0, 4);
            String last4 = cardNo.getText().substring(cardNo.getText().length() - 4);

            SaleService.Sale sale = new SaleService.Sale(
                    null,
                    customer.getCustomerID(),
                    CustomerEnums.PaymentMethod.CARD.name(),
                    vendor.getValue(),
                    first4,
                    last4,
                    YearMonth.of(cardExp.getValue().getYear(), cardExp.getValue().getMonth()),
                    LocalDateTime.now(),
                    draft.totalAmount()
            );
            int saleID = SaleService.recordSale(sale);
            for (SaleService.SaleItem item : draft.items()) {
                SaleService.recordSaleItem(new SaleService.SaleItem(
                        null,
                        saleID,
                        item.productID(),
                        item.quantity(),
                        item.price(),
                        item.subtotal()
                ));
            }
            SaleService.checkFlexiRateChange(customer, draft.totalAmount());

            Stage stage = (Stage) backButton.getScene().getWindow();
            new CommonCalls().traverse(stage, "/ui/sales/success.fxml", "Success");
        }
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/sales/selectItems.fxml"));
        Parent root = loader.load();

        SelectController controller = loader.getController();
        controller.cartCallback(checkCallback());

        stage.setTitle("Select Items");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }
}
