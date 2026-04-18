package org.novastack.iposca.cust.customer;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.utils.db.JooqConnection;

import java.util.ArrayList;

import static org.jooq.impl.DSL.max;
import static schema.tables.Customer.CUSTOMER;

/**
 * Class that contains the comprehensive business logic for customer handling. It mainly consists of customer database
 * CRUD operations.
 * */
public class Customer {
    private int customerID;
    private String name;
    private String email;
    private String address;
    private String phone;
    private float creditLimit;
    private String discountPlan;
    private String status;

    /**
     * Constructor for Customer class primarily used for customer registration.
     * @param name The name of the customer.
     * @param email The email of the customer.
     * @param address The address of the customer.
     * @param phone The phone number of the customer.
     * @param creditLimit The credit limit of the customer.
     * @param discountPlan The discount plan of the customer.
     * @param status The status of the customer.
     * */
    public Customer(String name, String email, String address, String phone, float creditLimit, String discountPlan, String status) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.creditLimit = creditLimit;
        this.discountPlan = discountPlan;
        this.status = status;
    }

    /**
     * Overloaded constructor for Customer class primarily used for customer retrieval.
     * @param customerID The ID of the customer.
     * @param name The name of the customer.
     * @param email The email of the customer.
     * @param address The address of the customer.
     * @param phone The phone number of the customer.
     * @param creditLimit The credit limit of the customer.
     * @param discountPlan The discount plan of the customer.
     * @param status The status of the customer.
     * */
    public Customer(int customerID, String name, String email, String address, String phone, float creditLimit, String discountPlan, String status) {
        this.customerID = customerID;
        this.name = name;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.creditLimit = creditLimit;
        this.discountPlan = discountPlan;
        this.status = status;
    }

    /**
     * Default constructor for Customer class.
     * */
    public Customer() {}

    /**
     * Adds a customer to the database.
     * @param customer The customer to be added.
     * @throws DataAccessException If there is an error in the database operation.
     * */
    public void addCustomer(Customer customer) throws DataAccessException {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(CUSTOMER)
                .set(CUSTOMER.NAME, customer.getName())
                .set(CUSTOMER.EMAIL, customer.getEmail())
                .set(CUSTOMER.ADDRESS, customer.getAddress())
                .set(CUSTOMER.PHONE, customer.getPhone())
                .set(CUSTOMER.CREDITLIMIT, customer.getCreditLimit())
                .set(CUSTOMER.DSCPLAN, customer.getDiscountPlan())
                .set(CUSTOMER.STATUS, customer.getStatus())
                .execute();
    }

    /**
     * Retrieves all the customers from the database.
     * @return An {@link ArrayList} of {@link Customer} objects, each representing a customer.
     * @throws DataAccessException If there is an error in the database operation.
     * */
    public ArrayList<Customer> getAllCustomers() throws DataAccessException {
        ArrayList<Customer> customers = new ArrayList<>();
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.selectFrom(CUSTOMER).fetch().forEach(record -> {
            customers.add(new Customer(
                    CUSTOMER.ID.getValue(record),
                    CUSTOMER.NAME.getValue(record),
                    CUSTOMER.EMAIL.getValue(record),
                    CUSTOMER.ADDRESS.getValue(record),
                    CUSTOMER.PHONE.getValue(record),
                    CUSTOMER.CREDITLIMIT.getValue(record),
                    CUSTOMER.DSCPLAN.getValue(record),
                    CUSTOMER.STATUS.getValue(record)));
        });
        return customers;
    }

    /**
     * Retrieve a specific customer from the database given their ID.
     * @param customerID The ID of the customer to retrieve.
     * @return A {@link Customer} object representing the customer.
     * @throws DataAccessException If there is an error in the database operation.
     * */
    public Customer getCustomer(int customerID) throws DataAccessException {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.selectFrom(CUSTOMER)
                .where(CUSTOMER.ID.eq(customerID))
                .fetchOne(record -> new Customer(
                        CUSTOMER.ID.getValue(record),
                        CUSTOMER.NAME.getValue(record),
                        CUSTOMER.EMAIL.getValue(record),
                        CUSTOMER.ADDRESS.getValue(record),
                        CUSTOMER.PHONE.getValue(record),
                        CUSTOMER.CREDITLIMIT.getValue(record),
                        CUSTOMER.DSCPLAN.getValue(record),
                        CUSTOMER.STATUS.getValue(record)));
    }

    /**
     * Edits a customer's details and updates the database.
     * @param customer The customer to update the details for.
     * @throws DataAccessException If there is an error in the database operation.
     * */
    public void editCustomer(Customer customer) throws DataAccessException {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.update(CUSTOMER)
                .set(CUSTOMER.NAME, customer.getName())
                .set(CUSTOMER.EMAIL, customer.getEmail())
                .set(CUSTOMER.ADDRESS, customer.getAddress())
                .set(CUSTOMER.PHONE, customer.getPhone())
                .set(CUSTOMER.CREDITLIMIT, customer.getCreditLimit())
                .set(CUSTOMER.DSCPLAN, customer.getDiscountPlan())
                .where(CUSTOMER.ID.eq(customer.getCustomerID()))
                .execute();
    }

    /**
     * Deletes an existing customer from the database given their ID.
     * @param id The ID of the customer to delete.
     * @throws DataAccessException If there is an error in the database operation.
     * */
    public void deleteCustomer(int id) throws DataAccessException {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.deleteFrom(CUSTOMER)
                .where(CUSTOMER.ID.eq(id))
                .execute();
    }

    /**
     * Dedicated method to update the account status of a customer, for the debt automation engine, given their ID.
     * @param customerID The ID of the customer to update status.
     * @param status The new status to update the customer to.
     * @throws DataAccessException If there is an error in the database operation.
     * */
    public static void updateAccountStatus(int customerID, String status) throws DataAccessException {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.update(CUSTOMER)
                .set(CUSTOMER.STATUS, status)
                .where(CUSTOMER.ID.eq(customerID))
                .execute();
    }

    /**
     * Method to get the customerID of a newly registered customer which takes advantage of primary key auto-increment
     * in SQLite. Used primarily for assignment of a discount plan during customer registration.
     * @return The latest customerID.
     * */
    public int getLatestID() {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.select(max(CUSTOMER.ID))
                .from(CUSTOMER)
                .fetchOneInto(Integer.class);
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public float getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(long creditLimit) {
        this.creditLimit = creditLimit;
    }

    public String getDiscountPlan() {
        return discountPlan;
    }

    public void setDiscountPlan(String discountPlan) {
        this.discountPlan = discountPlan;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
