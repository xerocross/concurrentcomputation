package com.adamfgcross.concurrentcomputations;

public class AuthenticationFailureException extends RuntimeException {
	private static final long serialVersionUID = 2530623620538241182L;

	public AuthenticationFailureException() {
        super();
    }

    // Constructor that accepts a custom error message
    public AuthenticationFailureException(String message) {
        super(message);
    }

    // Constructor that accepts a custom error message and a cause
    public AuthenticationFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor that accepts a cause
    public AuthenticationFailureException(Throwable cause) {
        super(cause);
    }
}
