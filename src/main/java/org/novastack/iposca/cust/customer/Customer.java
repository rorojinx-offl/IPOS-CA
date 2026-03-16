package org.novastack.iposca.cust.customer;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.utils.db.JooqConnection;

import java.util.ArrayList;

import static org.jooq.impl.DSL.max;
import static schema.tables.Customer.CUSTOMER;

public class Customer {
    private int customerID;
    private String name;
    private String email;
    private String address;
    private String phone;
    private float creditLimit;
    private String discountPlan;
    private String status;

    public Customer(String name, String email, String address, String phone, float creditLimit, String discountPlan, String status) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.creditLimit = creditLimit;
        this.discountPlan = discountPlan;
        this.status = status;
    }
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
    public Customer() {}

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

    public void deleteCustomer(int id) throws DataAccessException {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.deleteFrom(CUSTOMER)
                .where(CUSTOMER.ID.eq(id))
                .execute();
    }

    public static void updateAccountStatus(int customerID, String status) throws DataAccessException {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.update(CUSTOMER)
                .set(CUSTOMER.STATUS, status)
                .where(CUSTOMER.ID.eq(customerID))
                .execute();
    }

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
