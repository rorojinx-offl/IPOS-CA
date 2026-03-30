package org.novastack.iposca.rpt.model;

import java.time.LocalDate;

public class TurnoverData {
    private LocalDate reportPeriodStart;
    private LocalDate reportPeriodEnd;
    private float totalSalesAmount;
    private int totalSalesCount;
    private float totalOrdersPlacedValue;
    private String generatedBy;
    private LocalDate generatedTimestamp;

    public TurnoverData() {}

    public LocalDate getReportPeriodStart() { return reportPeriodStart; }
    public void setReportPeriodStart(LocalDate reportPeriodStart) { this.reportPeriodStart = reportPeriodStart; }

    public LocalDate getReportPeriodEnd() { return reportPeriodEnd; }
    public void setReportPeriodEnd(LocalDate reportPeriodEnd) { this.reportPeriodEnd = reportPeriodEnd; }

    public float getTotalSalesAmount() { return totalSalesAmount; }
    public void setTotalSalesAmount(float totalSalesAmount) { this.totalSalesAmount = totalSalesAmount; }

    public int getTotalSalesCount() { return totalSalesCount; }
    public void setTotalSalesCount(int totalSalesCount) { this.totalSalesCount = totalSalesCount; }

    public float getTotalOrdersPlacedValue() { return totalOrdersPlacedValue; }
    public void setTotalOrdersPlacedValue(float totalOrdersPlacedValue) { this.totalOrdersPlacedValue = totalOrdersPlacedValue; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public LocalDate getGeneratedTimestamp() { return generatedTimestamp; }
    public void setGeneratedTimestamp(LocalDate generatedTimestamp) { this.generatedTimestamp = generatedTimestamp; }


}
