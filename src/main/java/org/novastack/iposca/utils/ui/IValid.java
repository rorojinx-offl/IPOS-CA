package org.novastack.iposca.utils.ui;

public class IValid {
    public static boolean checkCreditLimit(String creditLimit) {
        float limit;
        try {
            limit = Float.parseFloat(creditLimit);
        } catch (NumberFormatException e) {
            return false;
        }
        return limit > 0;
    }

    public static boolean checkEmail(String email) {
        return email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    }

    public static boolean checkPhone(String phone) {
        return phone.matches("^\\d{11}$");
    }

    public static boolean checkRate(String rate) {
        int rateInt;
        try {
            rateInt = Integer.parseInt(rate);
        } catch (NumberFormatException e) {
            return false;
        }
        return rateInt >= 0 && rateInt <= 100;
    }

    public static boolean checkInt(String in) {
        try {
            Integer.parseInt(in);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
