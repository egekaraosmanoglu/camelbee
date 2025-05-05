package org.camelbee.security.routes.exception;

/**
 * Custom exception to indicate token validation failures.
 */
public class TokenValidationException extends RuntimeException {

  private final String errorCode;

  /**
   * Constructor with error code and message.
   *
   * @param errorCode the specific error code for the exception
   * @param message   the detail message
   */
  public TokenValidationException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  /**
   * Constructor with error code, message, and cause.
   *
   * @param errorCode the specific error code for the exception
   * @param message   the detail message
   * @param cause     the cause of the exception
   */
  public TokenValidationException(String errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  /**
   * Returns the error code associated with this exception.
   *
   * @return the error code
   */
  public String getErrorCode() {
    return errorCode;
  }

  @Override
  public String toString() {
    return "TokenValidationException{"
        + "errorCode=" + errorCode
        + ", message=" + getMessage()
        + '}';
  }
}