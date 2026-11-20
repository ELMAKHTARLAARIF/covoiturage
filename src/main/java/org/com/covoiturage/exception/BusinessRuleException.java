package org.com.covoiturage.exception;

public class BusinessRuleException extends RuntimeException {          // 409
    public BusinessRuleException(String message) { super(message); }
}