package org.camelbee.security.routes.exception;

/**
 * Custom exception to indicate authentication failures.
 */
public class AuthenticationFailedException extends RuntimeException {

  private final String errorCode;

  /**
   * Constructor with error code and message.
   *
   * @param errorCode the specific error code for the exception
   * @param message   the detail message
   */
  public AuthenticationFailedException(String errorCode, String message) {
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
  public AuthenticationFailedException(String errorCode, String message, Throwable cause) {
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
    return "AuthenticationFailedException{"
        + "errorCode=" + errorCode
        + ", message=" + getMessage()
        + '}';
  }
}