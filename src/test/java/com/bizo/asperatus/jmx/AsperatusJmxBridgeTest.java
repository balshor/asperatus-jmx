package com.bizo.asperatus.jmx;

import static com.bizo.asperatus.model.Unit.Count;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.bizo.asperatus.jmx.configuration.MetricConfiguration;
import com.google.common.collect.ImmutableList;


@SuppressWarnings({ "rawtypes", "unchecked" })
public class AsperatusJmxBridgeTest {
  private static String objectName(final int num) {
    return String.format("domain-%d: key1 = value1, key2 = value2", num);
  }

  private static final String attribute = "attribute";
  private static final String metric = "metric";
  private static final int baseFreq = 5;
  private static final String comment = "This is a test. This is only a test.";

  private MetricConfiguration config(final int num) {
    return new MetricConfiguration(objectName(num), attribute, metric, Count, baseFreq + num, comment);
  }

  private Runnable noop() {
    return new Runnable() {
      public void run() {
        // This is a noop.
      }
    };
  }

  private final List<MetricConfiguration> configs = ImmutableList.of(config(0), config(1), config(2), config(3));
  private final List<Runnable> noops = ImmutableList.of(noop(), noop(), noop(), noop());
  private final List<ScheduledFuture> futures = ImmutableList.of(
    mock(ScheduledFuture.class, "future0"),
    mock(ScheduledFuture.class, "future1"),
    mock(ScheduledFuture.class, "future2"),
    mock(ScheduledFuture.class, "future3"));

  private MetricRunnableFactory factory;
  private ScheduledExecutorService executor;
  private AsperatusJmxBridge bridge;

  @Before
  public void setUp() throws Exception {
    factory = mock(MetricRunnableFactory.class);
    executor = mock(ScheduledExecutorService.class);
    bridge = new AsperatusJmxBridge(factory, executor);

    for (int i = 0; i < configs.size(); i++) {
      when(factory.get(configs.get(i))).thenReturn(noops.get(i));
      when(executor.scheduleAtFixedRate(eq(noops.get(i)), anyInt(), anyInt(), any(TimeUnit.class))).thenReturn(
        futures.get(i));
    }
  }

  @Test
  public void testScheduling() throws Exception {
    bridge.monitor(ImmutableList.of(configs.get(0), configs.get(1)));

    verify(factory).get(configs.get(0));
    verify(factory).get(configs.get(1));
    verify(executor).scheduleAtFixedRate(noops.get(0), baseFreq, baseFreq, TimeUnit.SECONDS);
    verify(executor).scheduleAtFixedRate(noops.get(1), baseFreq + 1, baseFreq + 1, TimeUnit.SECONDS);
  }

  @Test
  public void testNewScheduling() throws Exception {
    bridge.monitor(ImmutableList.of(configs.get(0), configs.get(1)));

    verify(factory).get(configs.get(0));
    verify(factory).get(configs.get(1));
    verify(executor).scheduleAtFixedRate(noops.get(0), baseFreq, baseFreq, TimeUnit.SECONDS);
    verify(executor).scheduleAtFixedRate(noops.get(1), baseFreq + 1, baseFreq + 1, TimeUnit.SECONDS);

    bridge.monitor(ImmutableList.of(configs.get(2), configs.get(3)));

    verify(factory).get(configs.get(2));
    verify(factory).get(configs.get(3));
    verify(executor).scheduleAtFixedRate(noops.get(2), baseFreq + 2, baseFreq + 2, TimeUnit.SECONDS);
    verify(executor).scheduleAtFixedRate(noops.get(3), baseFreq + 3, baseFreq + 3, TimeUnit.SECONDS);
    verify(futures.get(0)).cancel(anyBoolean());
    verify(futures.get(1)).cancel(anyBoolean());
  }
}
