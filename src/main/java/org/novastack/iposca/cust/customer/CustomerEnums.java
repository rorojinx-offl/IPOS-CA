package org.novastack.iposca.cust.customer;

/**
 * A collection of enums that represent a defined set of data relating to a customer.
 * */
public class CustomerEnums {
    /**
     * The three possible statuses for a customer's reminder.
     * */
    public enum ReminderStatus {
        NO_NEED, DUE, SENT
    }

    /**
     * Reminders get sent twice per month, this enum represents the two types of reminders.
     * */
    public enum ReminderType {
        FIRST, SECOND
    }

    /**
     * The three payment methods a customer can use to complete a transaction.
     * */
    public enum PaymentMethod {
        CASH, CARD, CREDITS
    }

    /**
     * The three main card vendors, but also includes "Other" to cover any other vendor.
     * */
    public enum CardType {
        VISA, MASTERCARD, AMEX, OTHER
    }

    /**
     * Three possible statuses for a customer's account that can dictate their ability to make payments.
     * */
    public enum AccountStatus {
        NORMAL, SUSPENDED, IN_DEFAULT
    }

    /**
     * The two possible discount plans a customer can choose from.
     * */
    public enum DiscountPlan {
        FLEXIBLE, FIXED
    }
}
