package com.bizo.asperatus.jmx.configuration;

/**
 * This exception is thrown if a metric configuration is invalid for some reason.
 */
public class MetricConfigurationException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public MetricConfigurationException() {
  }

  public MetricConfigurationException(final String msg, final Throwable cause) {
    super(msg, cause);
  }

  public MetricConfigurationException(final String msg) {
    super(msg);
  }

  public MetricConfigurationException(final Throwable cause) {
    super(cause);
  }

}
