package org.novastack.iposca.exceptions;

/**
 * An exception class for authentication-related errors such as invalid credentials, existence of user or role changes.
 * */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}
