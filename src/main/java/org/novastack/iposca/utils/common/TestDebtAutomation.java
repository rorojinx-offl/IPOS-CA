package org.novastack.iposca.utils.common;

import org.novastack.iposca.cust.DAUtils;
import org.novastack.iposca.cust.DebtAutomationService;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;

public class TestDebtAutomation {
    static void main() {
        DebtAutomationService.runDebtEvaluation();
        DebtAutomationService.runDailyDebtEvaluation();
    }
}
