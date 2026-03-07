package org.novastack.iposca.utils.common;

import org.novastack.iposca.cust.CustomerMonthlySpend;
import org.novastack.iposca.cust.FlexiDiscountPlan;

import java.time.YearMonth;

public class TestMonthlySpend {
    static void main() {
        YearMonth monthYear = YearMonth.now();
        float thisSpend = 1000f;
        int id = 1;
        CustomerMonthlySpend cms = new CustomerMonthlySpend(id, monthYear, thisSpend);
        cms.recordSpend(cms);

        //Check if the new spend rate warrants a change in the flexi rate
        CustomerMonthlySpend.FlexiRateChange fxc = cms.warrantsRateChange(id);
        if (fxc.needChange()) {
            FlexiDiscountPlan fdp = new FlexiDiscountPlan(id, fxc.rate());
            fdp.modifyRate(fdp);
        } else {
            System.out.println("Rate stays");
        }
    }

}
