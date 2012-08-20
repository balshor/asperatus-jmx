package com.bizo.asperatus.jmx;

import static java.util.logging.Level.WARNING;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.MBeanServer;

import com.bizo.asperatus.jmx.configuration.MetricConfiguration;
import com.bizo.asperatus.logging.error.MachineInfo;
import com.bizo.asperatus.model.Dimension;
import com.bizo.asperatus.tracker.MetricTracker;

/**
 * This class allows setting a common MBeanServer, Dimensions, and ErrorHandler and constructing new MetricRunnables
 * from MetricConfigurations.
 */
public final class MetricRunnableFactoryImpl implements MetricRunnableFactory {
  private MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
  private final MetricTracker tracker;
  private List<Dimension> dimensions = MachineInfo.defaultDimensions;
  private ErrorHandler errorHandler = new LoggingErrorHandler(MetricRunnable.class, WARNING);

  public MetricRunnableFactoryImpl(final MetricTracker tracker) {
    this.tracker = tracker;
  }

  @Override
  public MetricRunnable get(final MetricConfiguration config) {
    return new MetricRunnable(config, mBeanServer, tracker, dimensions, errorHandler);
  }

  /**
   * Sets the MBeanServer from which data will be pulled. Defaults to the PlatformMBeanServer.
   */
  public void setMBeanServer(final MBeanServer mBeanServer) {
    this.mBeanServer = mBeanServer;
  }

  /**
   * Sets the dimensions to send to Asperatus. Defaults to MachineInfo.dimensions.
   */
  public void setDimensions(final List<Dimension> dimensions) {
    this.dimensions = dimensions;
  }

  /**
   * Sets the ErrorHandler that will receive exceptions. Defaults to a WARNING-level logging handler.
   */
  public void setErrorHandler(final ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
  }
}
