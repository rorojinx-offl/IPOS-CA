package org.novastack.iposca.cust;

import org.jooq.DSLContext;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.utils.db.JooqConnection;
import org.novastack.iposca.cust.customer.CustomerDebt;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import static schema.tables.CustomerDebt.CUSTOMER_DEBT;

public class DebtAutomationService {
    public static void runDebtEvaluation() {
        ArrayList<DAUtils.DebtWarrant> list = DAUtils.checkBalances();
        Month month = LocalDate.now().getMonth();
        LocalDate date = LocalDate.of(2026,month.getValue(),month.maxLength());
        for (DAUtils.DebtWarrant dw : list) {
            CustomerDebt debt = CustomerDebt.getDebtFull(dw.customerID());
            if (DAUtils.check1stReminderExists(debt)) {
                String firstDate = DAUtils.firstReminderPush(date);
                CustomerDebt.setReminderDateStatus(dw.customerID(), CustomerEnums.ReminderType.FIRST, firstDate, CustomerEnums.ReminderStatus.DUE.name());
            }
        }
    }

    public static void runDailyDebtEvaluation() {
        ArrayList<DAUtils.DebtWarrant> list = DAUtils.checkBalances();
        for (DAUtils.DebtWarrant dw : list) {
            CustomerDebt debt = CustomerDebt.getDebtFull(dw.customerID());
            if (DAUtils.check2ndReminderExists(debt)) {
                String secondDate = DAUtils.secondReminderPush(debt.getDate1Reminder());
                CustomerDebt.setReminderDateStatus(dw.customerID(), CustomerEnums.ReminderType.SECOND, secondDate, CustomerEnums.ReminderStatus.DUE.name());
                Customer.updateAccountStatus(dw.customerID(), Customer.AccountStatus.SUSPENDED.name());
                continue;
            }

            if (DAUtils.postSuspended(debt)) {
                Customer.updateAccountStatus(dw.customerID(), Customer.AccountStatus.IN_DEFAULT.name());
            }
        }

    }
}
