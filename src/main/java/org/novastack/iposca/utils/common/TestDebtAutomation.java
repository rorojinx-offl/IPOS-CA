package org.novastack.iposca.utils.common;

import org.novastack.iposca.cust.debt.DebtAutomationService;

public class TestDebtAutomation {
    static void main() {
        DebtAutomationService.runDebtEvaluation();
        DebtAutomationService.runDailyDebtEvaluation();
    }
}
