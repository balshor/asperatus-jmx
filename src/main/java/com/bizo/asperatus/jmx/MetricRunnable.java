package com.bizo.asperatus.jmx;

import java.util.List;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.bizo.asperatus.jmx.configuration.MetricConfiguration;
import com.bizo.asperatus.jmx.configuration.MetricConfigurationException;
import com.bizo.asperatus.model.Dimension;
import com.bizo.asperatus.tracker.MetricTracker;

/**
 * This runnable executes a single pull of data from JMX and pushes the information to Asperatus.
 */
public class MetricRunnable implements Runnable {
  private final MBeanServer mBeanServer;
  private final ObjectName jmxName;
  private final MetricTracker tracker;
  private final List<Dimension> dimensions;
  private final MetricConfiguration config;
  private final ErrorHandler errorHandler;

  /**
   * Creates a new MetricRunnable.
   * 
   * @param config
   *          the configuration to pull
   * @param server
   *          the MBeanServer containing data
   * @param tracker
   *          the Asperatus tracker that will receive data
   * @param dimensions
   *          the dimensions to send to Asperatus
   * @param errorHandler
   *          the handler that processes error notifications
   */
  public MetricRunnable(
      final MetricConfiguration config,
      final MBeanServer server,
      final MetricTracker tracker,
      final List<Dimension> dimensions,
      final ErrorHandler errorHandler) {
    mBeanServer = server;
    this.tracker = tracker;
    this.dimensions = dimensions;
    this.config = config;
    this.errorHandler = errorHandler;

    try {
      jmxName = new ObjectName(config.getObjectName());
    } catch (final MalformedObjectNameException moan) {
      throw new MetricConfigurationException(moan);
    }
  }

  @Override
  public void run() {
    try {
      final Object result = mBeanServer.getAttribute(jmxName, config.getAttribute());
      if (result instanceof Number) {
        tracker.track(config.getMetricName(), (Number) result, dimensions);
      } else {
        final String resultType = result != null ? result.getClass().getName() : "null";
        errorHandler.handleError(
          String.format("Metric %s returned a %s, required a Number", config.getMetricName(), resultType),
          null);
      }
    } catch (final Exception e) {
      errorHandler.handleError("Error while getting data for metric " + config.getMetricName(), e);
    }
  }
}
