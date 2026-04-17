package org.novastack.iposca.utils.common;

import org.novastack.iposca.cust.customer.CustomerDebt;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.cust.customer.CustomerPayment;

import java.time.LocalDate;

public class TestRepayments {
    static void main() {
        int customerID = 1;
        float repayment = 2900f;
        LocalDate date = LocalDate.now();
        String paymentMethod = CustomerEnums.PaymentMethod.CASH.name();

        CustomerDebt cd = new CustomerDebt().getDebtSimple(customerID);
        float balance = cd.getBalance();

        if (repayment >= balance) {
            CustomerPayment crp = new CustomerPayment(customerID, repayment, date, paymentMethod);
            crp.addRepayment(crp);
        } else {
            throw new RuntimeException("Repayment amount not paid in full");
        }
    }
}
