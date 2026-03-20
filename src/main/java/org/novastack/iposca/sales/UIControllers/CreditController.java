package org.novastack.iposca.sales.UIControllers;

import javafx.fxml.Initializable;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.sales.SaleService;

import java.net.URL;
import java.util.ResourceBundle;

public class CreditController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    private SaleService.SaleDraft draft;
    private Customer customer;

    public void receiver(SaleService.SaleDraft draft, Customer customer) {
        this.draft = draft;
        this.customer = customer;
    }
}
