package org.novastack.iposca.cust.reminders;

import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerDebt;
import org.novastack.iposca.cust.customer.CustomerEnums;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * A java bean to hold the required reminder data for reminder PDF generation by JasperReports
 * */
public class ReminderInfo {
    private String customerName;
    private String customerAddress;
    private String customerPhone;
    private YearMonth issueMonthYear; // The month of the billing cycle
    private LocalDate newDueDate; // The new due date for repayments
    private float debt; // The debt accumulated for the customer
    /**
     * A record used by all Jasper-facing factory or java bean classes to store merchant information.
     * @param name The name of the merchant.
     * @param address The address of the merchant.
     * @param email The email of the merchant.
     * @param logo The logo of the merchant represented as a byte array.
     * */
    public record Merchant(String name, String address, String email, byte[] logo) {}

    /**
     * Constructor for the ReminderInfo class.
     * @param customerName The name of the customer.
     * @param customerAddress The address of the customer.
     * @param customerPhone The phone number of the customer.
     * @param issueMonthYear The month of the billing cycle.
     * @param newDueDate The new due date for repayments.
     * @param debt The debt accumulated for the customer.
     * */
    public ReminderInfo(String customerName, String customerAddress, String customerPhone, YearMonth issueMonthYear, LocalDate newDueDate, float debt) {
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerPhone = customerPhone;
        this.issueMonthYear = issueMonthYear;
        this.newDueDate = newDueDate;
        this.debt = debt;
    }

    /**
     * Factory method to create a ReminderInfo object from a {@link CustomerDebt} object and a given reminder type.
     * From the {@link CustomerDebt} object, we retrieve the customer's ID and from that we can retrieve a {@link Customer}
     * object to get the customer's address and phone number. We also retrieve the customer's reminder date, based on
     * the provided reminder type. We then get the balance of the customer's debt.
     * @param cd The customer debt object.
     * @param type The type of reminder.
     * */
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
