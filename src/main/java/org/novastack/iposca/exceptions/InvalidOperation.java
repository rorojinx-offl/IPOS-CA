package org.novastack.iposca.exceptions;

/**
 * General exception for invalid operations made by the user.
 * */
public class InvalidOperation extends Exception {
    public InvalidOperation(String message) {
        super(message);
    }
}
