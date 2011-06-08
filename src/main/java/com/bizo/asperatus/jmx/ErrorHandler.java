package com.bizo.asperatus.jmx;

/**
 * Describes a handler that receives errors from asynchronous processes.
 */
public interface ErrorHandler {

  /**
   * Handle an error.
   * 
   * @param message
   *          a possibly null message
   * @param cause
   *          a possibly null throwable that caused the error
   */
  void handleError(String message, Throwable cause);

}
