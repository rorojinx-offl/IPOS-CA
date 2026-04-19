package org.novastack.iposca.exceptions;

/**
 * Exception class for bootstrap errors. Here, it extends {@link RuntimeException} instead of {@link Exception} because it is not expected to be caught.
 * */
public class BootstrapException extends RuntimeException {
    public BootstrapException(String message) {
        super(message);
    }
}
