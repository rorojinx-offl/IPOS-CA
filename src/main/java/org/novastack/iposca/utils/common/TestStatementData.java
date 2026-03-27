package org.novastack.iposca.utils.common;

import org.novastack.iposca.cust.statement.StatementInfo;
import org.novastack.iposca.cust.statement.StatementItems;
import org.novastack.iposca.cust.statement.StatementService;

import java.time.YearMonth;
import java.util.ArrayList;

public class TestStatementData {
    static void main() {
        //StatementService.trackMonthlyDebt(1, YearMonth.now().minusMonths(1), 1000f);
        StatementInfo info = StatementService.buildStatementData(1, YearMonth.now());

        if (info == null) {
            System.out.println("No data found!");
            return;
        }

        System.out.printf("""
                Customer Name: %s
                Billing Month: %s
                Balance: %f
                Items: %s
                """, info.getCustomer().getName(), info.getBillingMonth().toString(), info.getBalance(), csv(info.getItems()));
    }

    public static String csv(ArrayList<StatementItems> list) {
        StringBuilder sb = new StringBuilder();
        for (StatementItems items : list) {
            sb.append(items.getProductName()).append(", ");
        }
        return sb.toString();
    }
}
