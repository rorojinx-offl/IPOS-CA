package org.novastack.iposca.cust.reminders;

import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerDebt;
import org.novastack.iposca.cust.customer.CustomerEnums;

import java.time.LocalDate;
import java.time.YearMonth;

public class ReminderInfo {
    private String customerName;
    private String customerAddress;
    private String customerPhone;
    private YearMonth issueMonthYear;
    private LocalDate newDueDate;
    private float debt;
    public record Merchant(String name, String address, String email, byte[] logo) {}

    public ReminderInfo(String customerName, String customerAddress, String customerPhone, YearMonth issueMonthYear, LocalDate newDueDate, float debt) {
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerPhone = customerPhone;
        this.issueMonthYear = issueMonthYear;
        this.newDueDate = newDueDate;
        this.debt = debt;
    }

    public static ReminderInfo setReminderInfo(CustomerDebt cd, CustomerEnums.ReminderType type) {
        Customer c = new Customer().getCustomer(cd.getCustomerID());
        String address = c.getAddress();
        String phone = c.getPhone();
        LocalDate reminderDate = type.name().equals(CustomerEnums.ReminderType.FIRST.name()) ? cd.getDate1Reminder() : cd.getDate2Reminder();
        YearMonth issueMonthYear = YearMonth.of(reminderDate.getYear(), reminderDate.getMonth()).minusMonths(1);

        return new ReminderInfo(
                cd.getCustomerName(),
                address,
                phone,
                issueMonthYear,
                LocalDate.now().plusDays(7),
                cd.getBalance()
        );

    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public YearMonth getIssueMonthYear() {
        return issueMonthYear;
    }

    public LocalDate getNewDueDate() {
        return newDueDate;
    }

    public float getDebt() {
        return debt;
    }
}
