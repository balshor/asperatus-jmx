package com.bizo.asperatus.jmx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.bizo.asperatus.jmx.configuration.MetricConfiguration;
import com.bizo.asperatus.tracker.MetricTracker;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * This class handles scheduling the pulling of metrics from JMX and pushing to Asperatus.
 */
public class AsperatusJmxBridge {
  private final MetricRunnableFactory factory;
  private final AtomicReference<Collection<ScheduledFuture<?>>> currentlyRunning =
    new AtomicReference<Collection<ScheduledFuture<?>>>(ImmutableList.<ScheduledFuture<?>> of());
  private final ScheduledExecutorService executor;
  private final boolean ownsExecutor;

  /**
   * Creates a new brige from the platform MBeanServer to the given tracker.
   * 
   * @param tracker
   *          the Asperatus tracker that will receive metrics.
   */
  public AsperatusJmxBridge(final MetricTracker tracker) {
    this(new MetricRunnableFactoryImpl(tracker), null);
  }

  /**
   * Creates a new bridge based on the provided MetricRunnableFactory and ScheduledExecutorService.
   * 
   * @param factory
   *          the factory that will produce the runnables that pull and push data.
   * @param executor
   *          the scheduled executor service on which the runnables will be run.
   */
  public AsperatusJmxBridge(final MetricRunnableFactory factory, final ScheduledExecutorService executor) {
    this.factory = factory;
    if (executor != null) {
      this.executor = executor;
      ownsExecutor = false;
    } else {
      this.executor = defaultExecutor();
      ownsExecutor = true;
    }
  }

  private static final AtomicInteger sequenceNumGenerator = new AtomicInteger(0);

  /**
   * Constructs a default single-threaded scheduled executor service.
   */
  private static final ScheduledExecutorService defaultExecutor() {
    final ThreadFactory threadFactory = new ThreadFactory() {
      private static final String DEFAULT_THREAD_PREFIX = "asperatus-jmx-";

      @Override
      public Thread newThread(final Runnable r) {
        final int sequenceNum = sequenceNumGenerator.incrementAndGet();
        final String threadName = DEFAULT_THREAD_PREFIX + sequenceNum;
        final Thread t = new Thread(r, threadName);
        t.setDaemon(true);
        return t;
      }
    };
    return Executors.newSingleThreadScheduledExecutor(threadFactory);
  }

  /**
   * Sets the given futures as the currently running ones and cancels any previously running futures.
   */
  private void rotateFutures(final Collection<ScheduledFuture<?>> newFutures) {
    final Collection<ScheduledFuture<?>> previousFutures = currentlyRunning.getAndSet(newFutures);
    for (final ScheduledFuture<?> future : previousFutures) {
      future.cancel(false);
    }
  }

  /**
   * Starts monitoring the given configurations. Any previously monitored configurations will be cancelled.
   */
  public void monitor(final Collection<MetricConfiguration> configurations) {
    Preconditions.checkNotNull(configurations);
    final Collection<ScheduledFuture<?>> futures = new ArrayList<ScheduledFuture<?>>(configurations.size());
    for (final MetricConfiguration config : configurations) {
      final Runnable command = factory.get(config);
      final int freq = config.getFrequency();
      final ScheduledFuture<?> future = executor.scheduleAtFixedRate(command, freq, freq, TimeUnit.SECONDS);
      futures.add(future);
    }
    rotateFutures(futures);
  }

  /** Alias for monitor(configurations). */
  public void setConfigurations(final Collection<MetricConfiguration> configurations) {
    monitor(configurations);
  }

  /**
   * Stops monitoring the current configurations and shuts down any threads managed by this bridge.
   */
  public void shutdown() {
    rotateFutures(ImmutableList.<ScheduledFuture<?>> of());
    if (ownsExecutor) {
      executor.shutdown();
    }
  }

}
