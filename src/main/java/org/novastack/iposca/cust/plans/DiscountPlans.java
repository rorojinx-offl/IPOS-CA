package org.novastack.iposca.cust.plans;

import java.util.ArrayList;

public interface DiscountPlans {
    void modifyRate(DiscountPlans d);
    void addDiscount(DiscountPlans d);
    void removeDiscount(int d);
    ArrayList<DiscountPlans> getAllDiscounts();
    int getCustomerID();
    String getCustomerName();
    int getDiscountRate();

    public int getCurrentDiscountRate(int id);
}
