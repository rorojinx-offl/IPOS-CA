package org.novastack.iposca.sales.UIControllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.novastack.iposca.config.AppConfig;
import org.novastack.iposca.config.AppConfigAPI;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.reminders.ReminderInfo;
import org.novastack.iposca.sales.InvoiceFactory;
import org.novastack.iposca.sales.InvoiceItems;
import org.novastack.iposca.sales.SaleService;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SuccessController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    private Button okButton;

    private ObservableList<SaleLine> cartSession;
    private Customer customer;
    private SaleService.CartMode cartMode;
    private float total;
    private SelectController.Totals totals;

    public void receive(ObservableList<SaleLine> cart, Customer cust, SaleService.CartMode mode, SelectController.Totals totals) {
        cartSession = cart;
        customer = cust;
        cartMode = mode;
        this.totals = totals;
    }

    @FXML
    void printInvoice(MouseEvent event) throws IOException {
        if (cartMode == SaleService.CartMode.MEMBER && (cartSession == null || customer == null)) {
            new CommonCalls().openErrorDialog("Unable to print invoice: Cart session could not be found!");
            return;
        }

        if (!AppConfig.configExists()) {
            new CommonCalls().openErrorDialog("Document Template not configured. Please contact your admin/manager to configure it.");
            return;
        }

        ArrayList<InvoiceItems> items = new ArrayList<>();
        for (SaleLine item : cartSession) {
            items.add(new InvoiceItems(
                    item.getProduct().getName(),
                    item.getQuantity(),
                    (float) item.getUnitPrice(),
                    (float) item.getSubtotal()
            ));

            try {
                InvoiceFactory.InvoiceData data = new InvoiceFactory.InvoiceData(
                        items,
                        customer,
                        totals,
                        cartMode
                );

                ReminderInfo.Merchant merchant = new ReminderInfo.Merchant(
                        AppConfigAPI.decodeByteToString(AppConfig.get(AppConfig.ConfigKey.MERCHANT_NAME)),
                        AppConfigAPI.decodeByteToString(AppConfig.get(AppConfig.ConfigKey.MERCHANT_ADDRESS)),
                        AppConfigAPI.decodeByteToString(AppConfig.get(AppConfig.ConfigKey.MERCHANT_EMAIL)),
                        AppConfig.get(AppConfig.ConfigKey.MERCHANT_LOGO));
                InvoiceFactory.generateInvoice(data, merchant);
            } catch (Exception e) {
                new CommonCalls().openErrorDialog("Unable to print invoice: " + e.getMessage());
                return;
            }
        }
    }

    @FXML
    void returnToMenu(MouseEvent event) throws IOException {
        new CommonCalls().traverse((Stage) okButton.getScene().getWindow(), "/ui/sales/salesMenu.fxml", "Sales");
    }
}
