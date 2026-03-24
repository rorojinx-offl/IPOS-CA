package org.novastack.iposca.cust.statement;

import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.sales.SaleService;

import java.time.YearMonth;
import java.util.ArrayList;

public class StatementInfo {
    private Customer customer;
    private YearMonth prevBillingPeriod;
    private float balance;
    private ArrayList<SaleService.SaleItem> items;

    public StatementInfo(Customer customer, YearMonth prevBillingPeriod, float balance, ArrayList<SaleService.SaleItem> items) {
        this.customer = customer;
        this.prevBillingPeriod = prevBillingPeriod;
        this.balance = balance;
        this.items = items;
    }
}
