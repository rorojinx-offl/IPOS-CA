package org.novastack.iposca.cust.customer;

public class CustomerEnums {
    public enum ReminderStatus {
        NO_NEED, DUE, SENT
    }

    public enum ReminderType {
        FIRST, SECOND
    }

    public enum PaymentMethod {
        CASH, CARD
    }

    public enum CardType {
        VISA, MASTERCARD, AMEX, OTHER
    }

    public enum AccountStatus {
        NORMAL, SUSPENDED, IN_DEFAULT
    }

    public enum DiscountPlan {
        FLEXIBLE, FIXED
    }
}
