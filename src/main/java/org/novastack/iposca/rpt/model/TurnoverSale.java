package org.novastack.iposca.rpt.model;

public class TurnoverSale {
    private int saleId;
    private String paymentMethod;
    private String saleDate;
    private float amount;

    public TurnoverSale(int saleId, String paymentMethod, String saleDate, float amount) {
        this.saleId = saleId;
        this.paymentMethod = paymentMethod;
        this.saleDate = saleDate;
        this.amount = amount;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
