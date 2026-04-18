package org.novastack.iposca.cust.customer;

import org.jooq.DSLContext;
import org.jooq.TableField;
import org.novastack.iposca.sales.SaleService;
import org.novastack.iposca.utils.db.JooqConnection;
import schema.tables.records.CustomerDebtRecord;

import java.time.LocalDate;
import java.util.ArrayList;

import static schema.tables.CustomerDebt.CUSTOMER_DEBT;
import static schema.tables.Customer.CUSTOMER;

/**
 * Class that contains the comprehensive business logic for customer debt handling. Consists of complex database operations
 * to manage customer debt lifecycles at scale.
 * */
public class CustomerDebt {
    /** Customer that the debt belongs to.*/
    private int customerID;
    /** Debtor's name*/
    private String customerName;
    /** Debtor's credit limit*/
    private float custCreditLimit;
    /** Debtor's account status*/
    private String custStatus;
    /** Debtor's remaining balance*/
    private float balance;
    /** Reminder status for the first reminder*/
    private String status1Reminder;
    /** Reminder date for the first reminder*/
    private LocalDate date1Reminder;
    /** Reminder status for the second reminder*/
    private String status2Reminder;
    /** Reminder date for the second reminder*/
    private LocalDate date2Reminder;
    private LocalDate statusChangedAt;

    /**
     * Constructor for CustomerDebt class that's only used to get comprehensive information about a customer's debt in
     * {@link CustomerDebt#getDebtFull(int customerID)}
     * @param customerID The ID of the customer.
     * @param balance The remaining balance of the customer.
     * @param status1Reminder The status of the first reminder.
     * @param date1Reminder The date of the first reminder.
     * @param status2Reminder The status of the second reminder.
     * @param date2Reminder The date of the second reminder.
     * @param statusChangedAt The date when the status of the debt was last changed.
     * */
    public CustomerDebt(int customerID, float balance, String status1Reminder, LocalDate date1Reminder, String status2Reminder, LocalDate date2Reminder, LocalDate statusChangedAt) {
        this.customerID = customerID;
        this.balance = balance;
        this.status1Reminder = status1Reminder;
        this.date1Reminder = date1Reminder;
        this.status2Reminder = status2Reminder;
        this.date2Reminder = date2Reminder;
        this.statusChangedAt = statusChangedAt;
    }

    /**
     * Constructor for CustomerDebt class that's only used to get display information about a customer's debt in
     * {@link CustomerDebt#getAllDebts()}
     * @param customerID The ID of the customer.
     * @param customerName The name of the customer.
     * @param credLim The credit limit of the customer.
     * @param status The status of the customer.
     * @param balance The remaining balance of the customer.
     * @param status1Reminder The status of the first reminder.
     * @param date1Reminder The date of the first reminder.
     * @param status2Reminder The status of the second reminder.
     * @param date2Reminder The date of the second reminder.
     * @param statusChangedAt The date when the status of the debt was last changed.
     * */
    public CustomerDebt(int customerID, String customerName, float credLim, String status, float balance, String status1Reminder, LocalDate date1Reminder, String status2Reminder, LocalDate date2Reminder, LocalDate statusChangedAt) {
        this.customerID = customerID;
        this.customerName = customerName;
        this.custCreditLimit = credLim;
        this.custStatus = status;
        this.balance = balance;
        this.status1Reminder = status1Reminder;
        this.date1Reminder = date1Reminder;
        this.status2Reminder = status2Reminder;
        this.date2Reminder = date2Reminder;
        this.statusChangedAt = statusChangedAt;
    }

    /**
     * Constructor for CustomerDebt class that's used to get slimmer information about a customer's debt in
     * {@link CustomerDebt#getDebtSimple(int customerID)} and to upsert debt data into the database in
     * {@link CustomerDebt#upsertDebt(CustomerDebt cd, float creditLimit)} for the {@link org.novastack.iposca.sales.PaymentService}'s
     * credit transaction logic.
     * @param customerID The ID of the customer.
     * @param balance Acts as the transaction amount for the customer's recent payment for use in the upsert operation.
     */
    public CustomerDebt(int customerID, float balance) {
        this.customerID = customerID;
        this.balance = balance;
    }

    /** Default constructor for CustomerDebt class.*/
    public CustomerDebt() {}

    /** Performs an upsert operation on the database to update or insert a customer's debt record. This method is only
     * responsible for dealing with customer ID and balance, as the other fields are handled by other methods.
     * An initial balance is calculated based on the credit limit and the transaction amount when a new debt entry is
     * created.
     * @param cd The simple customer debt object to upsert.
     * @param creditLimit The credit limit of the customer.
     * */
    public void upsertDebt(CustomerDebt cd, float creditLimit) {
        DSLContext ctx = JooqConnection.getDSLContext();
        //Calculate initial balance based on credit limit and transaction amount for new debt entries.
        float initialBalance = creditLimit - cd.getBalance();
        ctx.insertInto(CUSTOMER_DEBT)
                .set(CUSTOMER_DEBT.CUST_ID, cd.getCustomerID())
                .set(CUSTOMER_DEBT.BALANCE, initialBalance)
                .onConflict(CUSTOMER_DEBT.CUST_ID)
                .doUpdate() //Update existing record if customer ID already exists.
                .set(CUSTOMER_DEBT.BALANCE, CUSTOMER_DEBT.BALANCE.minus(cd.getBalance()))
                .execute();
    }

    /**
     * Used primarily by the debt automation engine to update the status of a customer's debt. It is activated when a
     * customer has unpaid dues at the end of the month, when they haven't paid by the first reminder, or when they
     * haven't paid by the second reminder. {@link org.novastack.iposca.cust.UIControllers.DebtController} uses this
     * method to record reminders as sent after their successful generation. {@link org.novastack.iposca.cust.debt.DebtAutomationService}
     * uses this method to dictate when new reminders and account status changes are made. The method works by checking
     * the type of reminder issued and changing that reminder's status and date accordingly.
     * @param customerID The ID of the customer whose debt status is to be updated.
     * @param type The type of reminder that was sent.
     * @param date The date of the reminder that was sent.
     * @param status The status of the reminder that was sent.
     * */
    public static void setReminderDateStatus(int customerID, CustomerEnums.ReminderType type, String date, String status) {
        //Initialise the reminder date and status jOOQ columns to prepare for
        TableField<CustomerDebtRecord,String> colDate = null;
        TableField<CustomerDebtRecord,String> colStatus = null;

        //Based on the reminder type, assign the proper reminder date and status columns.
        switch (type) {
            case CustomerEnums.ReminderType.FIRST -> {
                colDate = CUSTOMER_DEBT.DATE_1_REMINDER;
                colStatus = CUSTOMER_DEBT.STATUS_1_REMINDER;
            }
            case CustomerEnums.ReminderType.SECOND -> {
                colDate = CUSTOMER_DEBT.DATE_2_REMINDER;
                colStatus = CUSTOMER_DEBT.STATUS_2_REMINDER;
            }
        }

        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.update(CUSTOMER_DEBT)
                .set(colDate, date)
                .set(colStatus, status)
                .where(CUSTOMER_DEBT.CUST_ID.eq(customerID))
                .execute();
    }

    /** Retrieve simple information about a customer's debt (only the customer ID and balance). This is primarily used
     * in {@link org.novastack.iposca.sales.PaymentService#processCreditPayment(SaleService.SaleDraft draft, Customer customer)}
     * @param customerID The ID of the customer whose debt information is to be retrieved.
     * @return A {@link CustomerDebt} object containing the customer ID and balance.
     * */
    public static CustomerDebt getDebtSimple(int customerID) {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.selectFrom(CUSTOMER_DEBT)
                .where(CUSTOMER_DEBT.CUST_ID.eq(customerID))
                .fetchOne(record -> new CustomerDebt(
                        CUSTOMER_DEBT.CUST_ID.getValue(record),
                        CUSTOMER_DEBT.BALANCE.getValue(record)
                ));
    }

    /**
     * Retrieve comprehensive information about a customer's debt. This method is used in
     * {@link org.novastack.iposca.cust.debt.DebtAutomationService} for the debt automation engine.
     * @param customerID The ID of the customer whose debt information is to be retrieved.
     * @return A {@link CustomerDebt} object containing the customer's debt information.
     * */
    public static CustomerDebt getDebtFull(int customerID) {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.selectFrom(CUSTOMER_DEBT)
                .where(CUSTOMER_DEBT.CUST_ID.eq(customerID))
                .fetchOne(record -> new CustomerDebt(
                        CUSTOMER_DEBT.CUST_ID.getValue(record),
                        CUSTOMER_DEBT.BALANCE.getValue(record),
                        CUSTOMER_DEBT.STATUS_1_REMINDER.getValue(record),
                        parseDate(CUSTOMER_DEBT.DATE_1_REMINDER.getValue(record)),
                        CUSTOMER_DEBT.STATUS_2_REMINDER.getValue(record),
                        parseDate(CUSTOMER_DEBT.DATE_2_REMINDER.getValue(record)),
                        parseDate(CUSTOMER_DEBT.STATUS_CHANGED_AT.getValue(record))
                ));
    }

    /**
     * Prepares debt data for a tabular representation of debt information in the debt management screen ({@link org.novastack.iposca.cust.UIControllers.DebtController}).
     * It expands the customer ID and uses it to retrieve the customer's name, credit limit, and account status using
     * SQL joins. It returns a list of {@link CustomerDebt} objects, each representing a customer's debt and is used
     * as the dataset for a JavaFX {@link javafx.scene.control.TableView}.
     * @return An {@link ArrayList} of {@link CustomerDebt} objects, each representing a customer's debt.
     * */
    public static ArrayList<CustomerDebt> getAllDebts() {
        ArrayList<CustomerDebt> debts = new ArrayList<>();
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.selectFrom(CUSTOMER_DEBT).fetch().forEach(record -> {
            //Retrieve customer name, credit limit, and account status using SQL joins.
            String custName = ctx.select(CUSTOMER.NAME)
                    .from(CUSTOMER_DEBT)
                    .join(CUSTOMER)
                    .on(CUSTOMER_DEBT.CUST_ID.eq(CUSTOMER.ID))
                    .where(CUSTOMER_DEBT.CUST_ID.eq(CUSTOMER_DEBT.CUST_ID.getValue(record)))
                    .fetchOne(CUSTOMER.NAME);

            //An object type of float is used instead of primitive float to cleanly handle null values.
            Float creditLimitRetr = ctx.select(CUSTOMER.CREDITLIMIT)
                            .from(CUSTOMER_DEBT)
                                    .join(CUSTOMER)
                                            .on(CUSTOMER_DEBT.CUST_ID.eq(CUSTOMER.ID))
                                                    .where(CUSTOMER_DEBT.CUST_ID.eq(CUSTOMER_DEBT.CUST_ID.getValue(record)))
                    .fetchOneInto(Float.class);

            //Should the Float object be null we can safely handle it and assign 0f to the primitive float
            float creditLimit = creditLimitRetr == null ? 0f : creditLimitRetr;

            String custStatus = ctx.select(CUSTOMER.STATUS)
                            .from(CUSTOMER_DEBT)
                                    .join(CUSTOMER)
                                            .on(CUSTOMER_DEBT.CUST_ID.eq(CUSTOMER.ID))
                                                    .where(CUSTOMER_DEBT.CUST_ID.eq(CUSTOMER_DEBT.CUST_ID.getValue(record)))
                                                            .fetchOne(CUSTOMER.STATUS);

            debts.add(new CustomerDebt(
                    CUSTOMER_DEBT.CUST_ID.getValue(record),
                    custName,
                    creditLimit,
                    custStatus,
                    creditLimit - CUSTOMER_DEBT.BALANCE.getValue(record), //Remember that we want to represent what the customer owes so far, not how much balance they have left
                    CUSTOMER_DEBT.STATUS_1_REMINDER.getValue(record),
                    parseDate(CUSTOMER_DEBT.DATE_1_REMINDER.getValue(record)),
                    CUSTOMER_DEBT.STATUS_2_REMINDER.getValue(record),
                    parseDate(CUSTOMER_DEBT.DATE_2_REMINDER.getValue(record)),
                    parseDate(CUSTOMER_DEBT.STATUS_CHANGED_AT.getValue(record))
            ));
        });
        return debts;
    }

    /**
     * Nullifies/erases data on a debt but does not erase it from the database. It is primarily used by {@link CustomerPayment}
     * to handle debt absolution for a customer when their account is in the in-default status and must be manually
     * restored to the normal status, so that their name remains in record.
     * @param customerID The ID of the customer whose account status is in-default.
     * */
    public static void dryDeleteDebt(int customerID) {
        DSLContext ctx = JooqConnection.getDSLContext();
        //Below are all stub or null values
        ctx.update(CUSTOMER_DEBT)
                .set(CUSTOMER_DEBT.BALANCE, 0f)
                .set(CUSTOMER_DEBT.STATUS_1_REMINDER, CustomerEnums.ReminderStatus.NO_NEED.name())
                .set(CUSTOMER_DEBT.DATE_1_REMINDER, (String) null)
                .set(CUSTOMER_DEBT.STATUS_2_REMINDER, CustomerEnums.ReminderStatus.NO_NEED.name())
                .set(CUSTOMER_DEBT.DATE_2_REMINDER, (String) null)
                .set(CUSTOMER_DEBT.STATUS_CHANGED_AT, (String) null)
                .where(CUSTOMER_DEBT.CUST_ID.eq(customerID))
                .execute();
    }

    /**
     * Deletes the debt entry of a given customer and is triggered by {@link CustomerPayment} when a customer's account
     * status is in normal or suspended, and they make a repayment, and {@link org.novastack.iposca.cust.UIControllers.DebtController}
     * when a dry-deleted debt of an in-default customer has their account restored to normal.
     * @param customerID The ID of the customer whose debt entry is to be deleted.
     * */
    public static void deleteDebt(int customerID) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.deleteFrom(CUSTOMER_DEBT)
                .where(CUSTOMER_DEBT.CUST_ID.eq(customerID))
                .execute();
    }

    /**
     * Central helper method for {@link CustomerDebt#getDebtFull(int customerID)} and {@link CustomerDebt#getDebtSimple(int customerID)}
     * to parse a date string from the database into a {@link LocalDate} object.
     * @param date The date string to be parsed.
     * @return A {@link LocalDate} object representing the parsed date.
     * */
    private static LocalDate parseDate(String date) {
        return date == null ? null : LocalDate.parse(date);
    }

    public int getCustomerID() {
        return customerID;
    }

    public float getBalance() {
        return balance;
    }

    public String getStatus1Reminder() {
        return status1Reminder;
    }

    public LocalDate getDate1Reminder() {
        return date1Reminder;
    }

    public String getStatus2Reminder() {
        return status2Reminder;
    }

    public LocalDate getDate2Reminder() {
        return date2Reminder;
    }

    public LocalDate getStatusChangedAt() {
        return statusChangedAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public float getCustCreditLimit() {
        return custCreditLimit;
    }

    public String getCustStatus() {
        return custStatus;
    }
}
