package org.novastack.iposca.cust.debt;

import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.cust.customer.CustomerDebt;
import org.novastack.iposca.cust.statement.StatementService;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;

public class DebtAutomationService {
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

            StatementService.trackMonthlyDebt(dw.customerID(), YearMonth.now(), dw.balance());
        }
    }

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
