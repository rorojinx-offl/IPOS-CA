package org.novastack.iposca.rpt.model;

import java.time.LocalDate;

public class DebtChangeData {
    private LocalDate startDate;
    private LocalDate endDate;
    private float openingAggregateDebt;
    private float paymentsReceived;
    private float newDebtAccrued;
    private float closingAggregateDebt;
    private int totalDebtorsCount;
    private int totalPaymentsCount;
    private int totalCreditSalesCount;
    private String generatedBy;
    private LocalDate generatedTimestamp;

    public DebtChangeData() {}


}
