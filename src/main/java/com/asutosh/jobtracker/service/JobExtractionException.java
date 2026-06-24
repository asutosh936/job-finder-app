package com.asutosh.jobtracker.service;

/**
 * Raised when a job posting URL cannot be fetched or parsed.
 */
public class JobExtractionException extends RuntimeException {

    public JobExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
