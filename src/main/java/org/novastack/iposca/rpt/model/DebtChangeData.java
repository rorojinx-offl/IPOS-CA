package org.novastack.iposca.rpt.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private List<DebtReportRow> pdfRows = new ArrayList<>();

    public DebtChangeData() {}

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public float getOpeningAggregateDebt() {
        return openingAggregateDebt;
    }

    public void setOpeningAggregateDebt(float openingAggregateDebt) {
        this.openingAggregateDebt = openingAggregateDebt;
    }

    public float getPaymentsReceived() {
        return paymentsReceived;
    }

    public void setPaymentsReceived(float paymentsReceived) {
        this.paymentsReceived = paymentsReceived;
    }

    public float getNewDebtAccrued() {
        return newDebtAccrued;
    }

    public void setNewDebtAccrued(float newDebtAccrued) {
        this.newDebtAccrued = newDebtAccrued;
    }

    public float getClosingAggregateDebt() {
        return closingAggregateDebt;
    }

    public void setClosingAggregateDebt(float closingAggregateDebt) {
        this.closingAggregateDebt = closingAggregateDebt;
    }

    public int getTotalDebtorsCount() {
        return totalDebtorsCount;
    }

    public void setTotalDebtorsCount(int totalDebtorsCount) {
        this.totalDebtorsCount = totalDebtorsCount;
    }

    public int getTotalPaymentsCount() {
        return totalPaymentsCount;
    }

    public void setTotalPaymentsCount(int totalPaymentsCount) {
        this.totalPaymentsCount = totalPaymentsCount;
    }

    public int getTotalCreditSalesCount() {
        return totalCreditSalesCount;
    }

    public void setTotalCreditSalesCount(int totalCreditSalesCount) {
        this.totalCreditSalesCount = totalCreditSalesCount;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public LocalDate getGeneratedTimestamp() {
        return generatedTimestamp;
    }

    public void setGeneratedTimestamp(LocalDate generatedTimestamp) {
        this.generatedTimestamp = generatedTimestamp;
    }

    public List<DebtReportRow> getPdfRows() {
        return pdfRows;
    }

    public void setPdfRows(List<DebtReportRow> pdfRows) {
        this.pdfRows = pdfRows;
    }
}
