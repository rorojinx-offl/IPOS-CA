package org.novastack.iposca.sales.UIControllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.novastack.iposca.cust.customer.Customer;
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

    public void receive(ObservableList<SaleLine> cart, Customer cust, SaleService.CartMode mode, float total) {
        cartSession = cart;
        customer = cust;
        cartMode = mode;
        this.total = total;
    }

    @FXML
    void printInvoice(MouseEvent event) throws IOException {
        if (cartMode == SaleService.CartMode.MEMBER && (cartSession == null || customer == null)) {
            new CommonCalls().openErrorDialog("Unable to print invoice: Cart session could not be found!");
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
                        total,
                        cartMode
                );
                InvoiceFactory.generateInvoice(data);
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
