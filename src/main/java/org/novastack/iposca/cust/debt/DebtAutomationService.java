package org.novastack.iposca.cust.debt;

import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.cust.customer.CustomerDebt;
import org.novastack.iposca.cust.statement.StatementService;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;

/**
 * Class that automates the debt evaluation process. It is responsible for checking the balance of each customer,
 * escalating the debt if necessary, setting reminders, and updating the customer's account status accordingly.
 * */
public class DebtAutomationService {
    /**
     * A monthly debt evaluation that checks the balance of each customer and initiates the debt escalation process. It
     * starts off by checking the balances in {@link DAUtils#checkBalances()} and then iterates through each customer's
     * debt. For each entry, we get the full debt info and check if the first reminder has been sent. If not, we push
     * the first reminder date forward by 5 days and set the reminder status to DUE. Then we also add this to the record
     * of monthly debt transactions in {@link StatementService}, which is to be used to generate monthly statements.
     * */
    public static void runDebtEvaluation() {
        ArrayList<DAUtils.DebtWarrant> list = DAUtils.checkBalances();
        if (list.isEmpty()) return;
        Month month = LocalDate.now().getMonth();
        //LocalDate date = LocalDate.of(2026,month.getValue(),month.maxLength());
        LocalDate date = LocalDate.now();
        for (DAUtils.DebtWarrant dw : list) {
            CustomerDebt debt = CustomerDebt.getDebtFull(dw.customerID());
            if (DAUtils.check1stReminderExists(debt)) {
                String firstDate = DAUtils.firstReminderPush(date);
                CustomerDebt.setReminderDateStatus(dw.customerID(), CustomerEnums.ReminderType.FIRST, firstDate, CustomerEnums.ReminderStatus.DUE.name());
            }

            Customer customer = new Customer().getCustomer(dw.customerID());
            float creditLimit = customer.getCreditLimit();

            StatementService.trackMonthlyDebt(dw.customerID(), YearMonth.now(), creditLimit - dw.balance());
        }
    }

    /**
     * This method is for continuing the debt evaluation process after the first reminder has been sent. It starts off by
     * fetching eligible debts, and for each entry, we check if the second reminder has been sent. If not, we push the
     * date for it to be sent forward by 15 days and set the reminder status to DUE, we also change the account status
     * from normal to suspended. If the second reminder has been sent, then we know that the debtor hasn't repaid despite
     * both reminders, so we change the account status to in-default.
     * */
    public static void runDailyDebtEvaluation() {
        ArrayList<DAUtils.DebtWarrant> list = DAUtils.checkBalances();
        if (list.isEmpty()) return;
        for (DAUtils.DebtWarrant dw : list) {
            CustomerDebt debt = CustomerDebt.getDebtFull(dw.customerID());
            if (DAUtils.check2ndReminderExists(debt)) {
                String secondDate = DAUtils.secondReminderPush(debt.getDate1Reminder());
                if (LocalDate.parse(secondDate).isAfter(LocalDate.now())) {
                    CustomerDebt.setReminderDateStatus(dw.customerID(), CustomerEnums.ReminderType.SECOND, secondDate, CustomerEnums.ReminderStatus.NO_NEED.name());
                } else {
                    CustomerDebt.setReminderDateStatus(dw.customerID(), CustomerEnums.ReminderType.SECOND, secondDate, CustomerEnums.ReminderStatus.DUE.name());
                }

                Customer.updateAccountStatus(dw.customerID(), CustomerEnums.AccountStatus.SUSPENDED.name());
                continue;
            }

            if (DAUtils.postSuspended(debt)) {
                Customer.updateAccountStatus(dw.customerID(), CustomerEnums.AccountStatus.IN_DEFAULT.name());
            }
        }

    }
}
