package com.asutosh.jobtracker.service;

/**
 * Raised when the master resume PDF cannot be parsed.
 */
public class ProfileException extends RuntimeException {

    public ProfileException(String message, Throwable cause) {
        super(message, cause);
    }
}
