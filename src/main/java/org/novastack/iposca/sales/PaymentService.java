package org.novastack.iposca.sales;

import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerCharge;
import org.novastack.iposca.cust.customer.CustomerDebt;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.exceptions.CreditPaymentException;

import java.time.LocalDate;

/**
 * Class that processes payments
 * */
public class PaymentService {
    /**
     * Method that processes a credit payment. First we check if the customer is authorised to make a credit payment.
     * Then we check if the customer has enough credits to make the payment. If so, we record the payment and update
     * their debt.
     * @param draft The draft sale to process.
     * @param customer The customer to process the payment for.
     * @throws CreditPaymentException If the customer is not authorised to make a credit payment, or if they don't have enough credits.
     * */
    public static void processCreditPayment(SaleService.SaleDraft draft, Customer customer) throws CreditPaymentException {
        if (draft == null || customer == null) {
            throw new CreditPaymentException("Unable to process customer and sales data!");
        }

        int custID = customer.getCustomerID();
        float transactionAmount = draft.totalAmount();
        String status = customer.getStatus();
        float creditLimit = customer.getCreditLimit();
        if (status.equals(CustomerEnums.AccountStatus.SUSPENDED.name()) || status.equals(CustomerEnums.AccountStatus.IN_DEFAULT.name())) {
            throw new CreditPaymentException("Customer is not authorised to make a credit payment, their account is currently: " + status);
        }

        CustomerDebt check = CustomerDebt.getDebtSimple(custID);
        if (check == null) {
            if (creditLimit < transactionAmount) {
                throw new CreditPaymentException("Not enough credits to make this payment!");
            }
        } else {
            if (check.getBalance() < transactionAmount) {
                throw new CreditPaymentException("Not enough credits to make this payment!");
            }
        }

        CustomerCharge cc = new CustomerCharge(custID, transactionAmount, LocalDate.now());
        cc.recordCharge(cc);


        CustomerDebt cd = new CustomerDebt(custID, transactionAmount);
        cd.upsertDebt(cd, creditLimit);


    }
}
