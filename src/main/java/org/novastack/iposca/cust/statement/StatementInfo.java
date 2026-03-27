package org.novastack.iposca.cust.statement;

import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.sales.InvoiceItems;
import org.novastack.iposca.sales.SaleService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;

public class StatementInfo {
    private Customer customer;
    private YearMonth billingMonth;
    private float balance;
    private ArrayList<StatementItems> items;

    public StatementInfo(Customer customer, YearMonth billingMonth, float balance, ArrayList<StatementItems> items) {
        this.customer = customer;
        this.billingMonth = billingMonth;
        this.balance = balance;
        this.items = items;
    }

    public Customer getCustomer() {
        return customer;
    }

    public YearMonth getBillingMonth() {
        return billingMonth;
    }

    public float getBalance() {
        return balance;
    }

    public ArrayList<StatementItems> getItems() {
        return items;
    }
}
