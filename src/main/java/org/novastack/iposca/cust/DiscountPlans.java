package org.novastack.iposca.cust;

import java.util.ArrayList;

public interface DiscountPlans {
    void modifyRate(DiscountPlans d);
    void addDiscount(DiscountPlans d);
    void removeDiscount(int d);
    ArrayList<DiscountPlans> getAllDiscounts();
    int getCustomerID();
    int getDiscountRate();
}
