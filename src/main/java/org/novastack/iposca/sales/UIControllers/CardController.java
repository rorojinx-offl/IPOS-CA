package org.novastack.iposca.sales.UIControllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
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
    private SaleService.CartMode cartMode;
    private float total;


    public void receive(Customer cust, SaleService.SaleDraft sd, ObservableList<SaleLine> cs, SaleService.CartMode mode, float total) {
        customer = cust;
        draft = sd;
        cartSession = cs;
        cartMode = mode;
        this.total = total;
    }

    public SaleService.Callback checkCallback() {
        Customer customer = cartMode == SaleService.CartMode.MEMBER ? this.customer : null;

        if (cartSession != null && cartMode != null) {
            return new SaleService.Callback(customer, cartSession, cartMode);
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

            if (cartMode == SaleService.CartMode.MEMBER && (customer == null || draft == null)) {
                new CommonCalls().openErrorDialog("Unable to parse customer or order!");
                Stage stage = (Stage) backButton.getScene().getWindow();
                new CommonCalls().traverse(stage, "/ui/sales/salesAccount.fxml", "Sale for Account Holder");
                return;
            }

            if (cartMode == SaleService.CartMode.MEMBER && !customer.getStatus().equals(CustomerEnums.AccountStatus.NORMAL.name())) {
                new CommonCalls().openErrorDialog("Customer is not authorised to make payments!");
                return;
            }

            String first4 = cardNo.getText().substring(0, 4);
            String last4 = cardNo.getText().substring(cardNo.getText().length() - 4);

            Integer customerID = cartMode == SaleService.CartMode.MEMBER ? customer.getCustomerID() : null;

            SaleService.Sale sale = new SaleService.Sale(
                    null,
                    customerID,
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
            if (cartMode == SaleService.CartMode.MEMBER) SaleService.checkFlexiRateChange(customer, draft.totalAmount());

            Stage stage = (Stage) backButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/sales/success.fxml"));
            Parent root = loader.load();

            SuccessController controller = loader.getController();
            controller.receive(cartSession, customer, cartMode, draft.totalAmount());

            stage.setTitle("Payment Successful");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
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
