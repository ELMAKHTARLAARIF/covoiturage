package org.com.covoiturage.exception;

public class ForbiddenException extends RuntimeException {             // 403
  public ForbiddenException(String message) { super(message); }
}