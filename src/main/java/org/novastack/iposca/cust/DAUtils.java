package org.novastack.iposca.cust;

import org.jooq.DSLContext;
import org.novastack.iposca.cust.customer.CustomerDebt;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.utils.db.JooqConnection;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;

import static schema.tables.CustomerDebt.CUSTOMER_DEBT;

public class DAUtils {
    protected record DebtWarrant(int customerID, float balance) {}

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

    protected static boolean check1stReminderExists(org.novastack.iposca.cust.customer.CustomerDebt debt) {
        return debt.getDate1Reminder() == null && debt.getStatus1Reminder() == null;
    }

    protected static boolean check2ndReminderExists(org.novastack.iposca.cust.customer.CustomerDebt debt) {
        return (debt.getStatus1Reminder().equals(CustomerEnums.ReminderStatus.SENT.name())) && debt.getDate2Reminder() == null && debt.getStatus2Reminder() == null;
    }

    protected static boolean isLastDay() {
        LocalDate date = LocalDate.now();
        Month month = date.getMonth();
        return month.maxLength() == date.getDayOfMonth();
    }

    protected static String firstReminderPush(LocalDate date) {
        return date.plusDays(5).toString();
    }

    protected static boolean postSuspended(CustomerDebt debt) {
        String status = debt.getStatus2Reminder() == null ? "null" : debt.getStatus2Reminder();
        //LocalDate testDate = LocalDate.of(2026, debt.getDate2Reminder().getMonthValue(), debt.getDate2Reminder().getMonth().maxLength());
        return status.equals(CustomerEnums.ReminderStatus.SENT.name()) && debt.getDate2Reminder() != null && LocalDate.now().getDayOfMonth() == debt.getDate2Reminder().getMonth().maxLength();
    }

    protected static String secondReminderPush(LocalDate firstSentDate) {
        return firstSentDate.plusDays(15).toString();
    }

}
