package org.novastack.iposca.cust.customer;

import org.jooq.DSLContext;
import org.novastack.iposca.utils.db.JooqConnection;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;

import static schema.tables.Customer.CUSTOMER;
import static schema.tables.CustomerReminder.CUSTOMER_REMINDER;

public class CustomerReminder {
    private int custID;
    private String customerName;
    private Month billingMonth;
    private CustomerEnums.ReminderType reminderType;
    private LocalDate reminderDate;

    public CustomerReminder(int custID, Month billingMonth, CustomerEnums.ReminderType reminderType, LocalDate reminderDate) {
        this.custID = custID;
        this.billingMonth = billingMonth;
        this.reminderType = reminderType;
        this.reminderDate = reminderDate;
    }

    public CustomerReminder(int custID, String customerName, Month billingMonth, CustomerEnums.ReminderType reminderType, LocalDate reminderDate) {
        this.custID = custID;
        this.customerName = customerName;
        this.billingMonth = billingMonth;
        this.reminderType = reminderType;
        this.reminderDate = reminderDate;
    }

    public void recordReminder(CustomerReminder cr) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(CUSTOMER_REMINDER)
                .set(CUSTOMER_REMINDER.CUST_ID, cr.getCustID())
                .set(CUSTOMER_REMINDER.BILLING_MONTH, cr.getBillingMonth().toString())
                .set(CUSTOMER_REMINDER.REMINDER_TYPE, cr.getReminderType().name())
                .set(CUSTOMER_REMINDER.GENERATED_DATE, cr.getReminderDate().toString())
                .execute();
    }

    public static ArrayList<CustomerReminder> getAllReminders() {
        ArrayList<CustomerReminder> reminders = new ArrayList<>();
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.selectFrom(CUSTOMER_REMINDER).fetch().forEach(record -> {
            String custName = ctx.select(CUSTOMER.NAME)
                    .from(CUSTOMER_REMINDER)
                    .join(CUSTOMER)
                    .on(CUSTOMER_REMINDER.CUST_ID.eq(CUSTOMER.ID))
                    .where(CUSTOMER_REMINDER.CUST_ID.eq(CUSTOMER_REMINDER.CUST_ID.getValue(record)))
                    .fetchOne(CUSTOMER.NAME);

            reminders.add(new CustomerReminder(
                    CUSTOMER_REMINDER.CUST_ID.getValue(record),
                    custName,
                    Month.valueOf(CUSTOMER_REMINDER.BILLING_MONTH.getValue(record)),
                    CustomerEnums.ReminderType.valueOf(CUSTOMER_REMINDER.REMINDER_TYPE.getValue(record)),
                    LocalDate.parse(CUSTOMER_REMINDER.GENERATED_DATE.getValue(record))
            ));
        });
        return reminders;
    }

    public int getCustID() {
        return custID;
    }

    public Month getBillingMonth() {
        return billingMonth;
    }

    public CustomerEnums.ReminderType getReminderType() {
        return reminderType;
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }
    public String getCustomerName() {
        return customerName;
    }
}
