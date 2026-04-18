package org.novastack.iposca.cust.debt;

import org.jooq.DSLContext;
import org.novastack.iposca.cust.customer.CustomerDebt;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.utils.db.JooqConnection;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;

import static schema.tables.CustomerDebt.CUSTOMER_DEBT;

/**
 * Utility class for {@link DebtAutomationService}
 * */
public class DAUtils {
    /**
     * Record that stores the customer ID and balance of a debt that requires a state change.
     * */
    protected record DebtWarrant(int customerID, float balance) {}

    /**
     * Checks balances of all customers with unpaid debts and returns a list of those customers.
     * @return A list of {@link DebtWarrant} objects representing customers with pending balances.
     * */
    protected static ArrayList<DebtWarrant> checkBalances() {
        ArrayList<DebtWarrant> balances = new ArrayList<>();
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.select(CUSTOMER_DEBT.CUST_ID,CUSTOMER_DEBT.BALANCE)
                .from(CUSTOMER_DEBT)
                .where(CUSTOMER_DEBT.BALANCE.greaterThan(0f))
                .fetch().forEach(record -> balances.add(new DebtWarrant(
                        CUSTOMER_DEBT.CUST_ID.getValue(record),
                        CUSTOMER_DEBT.BALANCE.getValue(record)
                )));
        return balances;
    }

    /**
     * Checks if a first reminder hasn't been issued yet by checking if the reminder status and date are null.
     * @param debt The customer's debt to check.
     * @return True if the first reminder hasn't been issued yet, false otherwise.
     * */
    protected static boolean check1stReminderExists(org.novastack.iposca.cust.customer.CustomerDebt debt) {
        return debt.getDate1Reminder() == null && debt.getStatus1Reminder() == null;
    }

    /**
     * Checks if a second reminder hasn't been issued yet. The first requirement is that there is atleast a status for
     * the first reminder. The second requirement is that the first reminder has the status of SENT. The final requirement
     * is that the second reminder is null.
     * @param debt The customer's debt to check.
     * @return True if the second reminder hasn't been issued yet, false otherwise.
     * */
    protected static boolean check2ndReminderExists(org.novastack.iposca.cust.customer.CustomerDebt debt) {
        if (debt.getStatus1Reminder() == null) return false;
        return (debt.getStatus1Reminder().equals(CustomerEnums.ReminderStatus.SENT.name())) && debt.getDate2Reminder() == null && debt.getStatus2Reminder() == null;
    }

    /**
     * Pushes the issuance of the first reminder forward by 5 days from when an unpaid debt has been escalated.
     * @param date The date of the escalation.
     * @return The string of the date of the first reminder.
     * */
    protected static String firstReminderPush(LocalDate date) {
        return date.plusDays(5).toString();
    }

    /**
     * Check that the second reminder has been sent and that it is the last day of the month, so that if the debt still
     * remains unpaid, the account status of the debtor will change from suspended to in-default.
     * @param debt The customer's debt to check.
     * @return True if the second reminder has been sent and is the last day of the month, false otherwise.
     * */
    protected static boolean postSuspended(CustomerDebt debt) {
        String status = debt.getStatus2Reminder() == null ? "null" : debt.getStatus2Reminder();
        //LocalDate testDate = LocalDate.of(2026, debt.getDate2Reminder().getMonthValue(), debt.getDate2Reminder().getMonth().maxLength());
        return status.equals(CustomerEnums.ReminderStatus.SENT.name()) && debt.getDate2Reminder() != null && LocalDate.now().getDayOfMonth() == debt.getDate2Reminder().getMonth().maxLength();
    }

    /**
     * Pushes the issuance of the second reminder forward by 15 days from when the first reminder has been sent.
     * @param firstSentDate The date of the first reminder.
     * @return The string of the date of the second reminder.
     * */
    protected static String secondReminderPush(LocalDate firstSentDate) {
        return firstSentDate.plusDays(15).toString();
    }

}
