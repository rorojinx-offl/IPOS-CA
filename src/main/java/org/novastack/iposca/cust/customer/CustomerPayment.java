package org.novastack.iposca.cust.customer;

import org.jooq.DSLContext;
import org.novastack.iposca.cust.statement.StatementService;
import org.novastack.iposca.utils.db.JooqConnection;

import static schema.tables.CustomerRepayment.CUSTOMER_REPAYMENT;
import static schema.tables.Customer.CUSTOMER;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Class that records customer payments and absolves debt status to applicable customers.
 * */
public class CustomerPayment {
    private int customerID;
    private float amount;
    private LocalDate date;
    private String paymentMethod;
    private String cardType;
    private String cardFirst4;
    private String cardLast4;
    private String cardExp;

    /**
     * Constructor for CustomerPayment class when the customer repays with a card.
     * */
    public CustomerPayment(int customerID, float amount, LocalDate date, String paymentMethod, String cardType, String cardFirst4, String cardLast4, String cardExp) {
        this.customerID = customerID;
        this.amount = amount;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.cardType = cardType;
        this.cardFirst4 = cardFirst4;
        this.cardLast4 = cardLast4;
        this.cardExp = cardExp;
    }

    /**
     * Constructor for CustomerPayment class when the customer repays with cash.
     * */
    public CustomerPayment(int customerID, float amount, LocalDate date, String paymentMethod) {
        this.customerID = customerID;
        this.amount = amount;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.cardType = null;
        this.cardFirst4 = null;
        this.cardLast4 = null;
        this.cardExp = null;
    }

    /**
     * Records a successful debt repayment and absolves the customer's debt status.
     * @param crp The customer payment info to be recorded.
     * */
    public void addRepayment(CustomerPayment crp) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(CUSTOMER_REPAYMENT)
                .set(CUSTOMER_REPAYMENT.CUST_ID, crp.getCustomerID())
                .set(CUSTOMER_REPAYMENT.AMOUNT, crp.getAmount())
                .set(CUSTOMER_REPAYMENT.DATE, crp.getDate().toString())
                .set(CUSTOMER_REPAYMENT.METHOD, crp.getPaymentMethod())
                .set(CUSTOMER_REPAYMENT.CARD_TYPE, crp.getCardType())
                .set(CUSTOMER_REPAYMENT.CARD_FIRST4, crp.getCardFirst4())
                .set(CUSTOMER_REPAYMENT.CARD_LAST4, crp.getCardLast4())
                .set(CUSTOMER_REPAYMENT.CARD_EXP, crp.getCardExp())
                .execute();

        //After the repayment is recorded, the customer's debt status is absolved.'
        absolveDebt(crp.getCustomerID());
    }

    /**
     * Revokes the customer's debt status by deleting the debt entry and deleting the month's data (so that a monthly
     * statement can't be generated for the customer). If the customer is in default, the debt entry is nullified, maintaining
     * their name on the debt list to allow for the restoration of their account status.
     * @param customerID The ID of the customer whose debt status is to be absolved.
     * */
    private void absolveDebt(int customerID) {
        DSLContext ctx = JooqConnection.getDSLContext();
        //Get the customer's current status so that we can decide how to delete the debt.
        String custStatus = ctx.select(CUSTOMER.STATUS)
                .from(CUSTOMER)
                .where(CUSTOMER.ID.eq(customerID))
                .fetchOneInto(String.class);

        custStatus = custStatus == null ? "" : custStatus;

        /*If the customer is in default, we don't need to delete the debt entry but instead remove data on it so that
        * their name remains on the debt table to allow for account restoration.*/
        if (custStatus.equals(CustomerEnums.AccountStatus.IN_DEFAULT.name())) {
            org.novastack.iposca.cust.customer.CustomerDebt.dryDeleteDebt(customerID);
            return;
        }

        org.novastack.iposca.cust.customer.CustomerDebt.deleteDebt(customerID);
        StatementService.deleteMonthData(customerID, YearMonth.now().minusMonths(1));
    }

    public int getCustomerID() {
        return customerID;
    }

    public float getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getCardType() {
        return cardType;
    }

    public String getCardFirst4() {
        return cardFirst4;
    }

    public String getCardLast4() {
        return cardLast4;
    }

    public String getCardExp() {
        return cardExp;
    }
}
