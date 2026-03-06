package org.novastack.iposca.utils.ui;

public class IValid {
    public boolean checkCreditLimit(String creditLimit) {
        float limit;
        try {
            limit = Float.parseFloat(creditLimit);
        } catch (NumberFormatException e) {
            return false;
        }
        return limit > 0;
    }

    public boolean checkEmail(String email) {
        return email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    }

    public boolean checkPhone(String phone) {
        return phone.matches("^\\d{11}$");
    }
}
