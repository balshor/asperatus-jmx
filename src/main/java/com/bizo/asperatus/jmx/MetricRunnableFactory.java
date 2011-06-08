package com.bizo.asperatus.jmx;

import com.bizo.asperatus.jmx.configuration.MetricConfiguration;

/**
 * This interface describes something that creates MetricRunnables for given configurations.
 */
public interface MetricRunnableFactory {
  Runnable get(final MetricConfiguration config);
}
