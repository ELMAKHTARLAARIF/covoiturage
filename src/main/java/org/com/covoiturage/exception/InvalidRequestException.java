package org.com.covoiturage.exception;

public class InvalidRequestException extends RuntimeException {        // 400
    public InvalidRequestException(String message) { super(message); }
}