package org.com.covoiturage.exception;

public class ResourceNotFoundException extends RuntimeException {      // 404
    public ResourceNotFoundException(String message) { super(message); }
}