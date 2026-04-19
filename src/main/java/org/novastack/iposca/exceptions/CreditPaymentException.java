package org.novastack.iposca.exceptions;

/**
 * Exception class for credit payment failures in {@link org.novastack.iposca.sales.PaymentService}.
 * */
public class CreditPaymentException extends Exception {
    public CreditPaymentException(String message) {
        super(message);
    }
}
