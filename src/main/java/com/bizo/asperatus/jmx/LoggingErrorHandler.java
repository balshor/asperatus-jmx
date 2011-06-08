package com.bizo.asperatus.jmx;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This simple implementation of an ErrorHandler simply logs errors to the given logger at the specified fixed logging
 * level.
 */
public class LoggingErrorHandler implements ErrorHandler {
  private final Logger logger;
  private final Level level;

  public LoggingErrorHandler(final Logger logger, final Level level) {
    this.logger = logger;
    this.level = level;
  }

  public LoggingErrorHandler(final Class<?> cls, final Level level) {
    logger = Logger.getLogger(cls.getName());
    this.level = level;
  }

  @Override
  public void handleError(final String message, final Throwable cause) {
    if (cause != null) {
      logger.log(level, message, cause);
    } else {
      logger.log(level, message);
    }
  }
}
