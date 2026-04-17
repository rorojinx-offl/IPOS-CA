package org.novastack.iposca.utils.common;

import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerCharge;
import org.novastack.iposca.cust.customer.CustomerDebt;
import org.novastack.iposca.cust.customer.CustomerEnums;

import java.time.LocalDate;

public class TestCreditDebt {
    static void main() {
        float transactionAmount = 100f;
        int customerID = 1;
        Customer customer = new Customer();
        customer = customer.getCustomer(customerID);

        CustomerCharge cc = new CustomerCharge(customerID, transactionAmount, LocalDate.now());
        cc.recordCharge(cc);

        float credLimit = customer.getCreditLimit();
        String status = customer.getStatus();
        if (status.equals(CustomerEnums.AccountStatus.SUSPENDED.name()) || status.equals(CustomerEnums.AccountStatus.IN_DEFAULT.name())) {
            throw new RuntimeException("Customer is not authorised to make a credit payment");
        }
        CustomerDebt cd = new CustomerDebt(customerID, transactionAmount);
        cd.upsertDebt(cd, credLimit);
    }
}
