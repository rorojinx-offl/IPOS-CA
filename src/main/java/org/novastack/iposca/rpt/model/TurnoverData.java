package org.novastack.iposca.rpt.model;

import java.time.LocalDate;

public class TurnoverData {
    private LocalDate reportPeriodStart;
    private LocalDate reportPeriodEnd;
    private float totalSalesAmount;
    private int totalSalesCount;
    private float totalOrdersPlacedValue;
    private String generatedBy;
    private LocalDate generatedTimeStamp;

    public TurnoverData() {}
}
