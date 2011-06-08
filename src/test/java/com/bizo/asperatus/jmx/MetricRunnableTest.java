package com.bizo.asperatus.jmx;

import static com.bizo.asperatus.model.Unit.Count;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;

import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Before;
import org.junit.Test;

import com.bizo.asperatus.jmx.configuration.MetricConfiguration;
import com.bizo.asperatus.model.Dimension;
import com.bizo.asperatus.tracker.MetricTracker;
import com.google.common.collect.ImmutableList;

@SuppressWarnings("unchecked")
public class MetricRunnableTest {
  private static final String objectName = "domain: key1 = value1, key2 = value2";
  private static final String attribute = "attribute";
  private static final String metric = "metric";
  private static final int frequency = 5;
  private static final String comment = "This is a test. This is only a test.";
  private final MetricConfiguration config = new MetricConfiguration(
    objectName,
    attribute,
    metric,
    Count,
    frequency,
    comment);

  private final List<Dimension> dimensions = ImmutableList.of(new Dimension("key", "value"));
  private MBeanServer server;
  private MetricTracker tracker;
  private ErrorHandler handler;
  private MetricRunnable runnable;

  @Before
  public void setUp() throws Exception {
    server = mock(MBeanServer.class);
    tracker = mock(MetricTracker.class);
    handler = mock(ErrorHandler.class);
    runnable = new MetricRunnable(config, server, tracker, dimensions, handler);
  }

  @Test
  public void testValidMetric() throws Exception {
    when(server.getAttribute(new ObjectName(objectName), attribute)).thenReturn(100);

    runnable.run();

    verify(tracker).track("metric", 100, dimensions);
    verify(handler, never()).handleError(anyString(), any(Throwable.class));
  }

  @Test
  public void testNonnumericMetric() throws Exception {
    when(server.getAttribute(new ObjectName(objectName), attribute)).thenReturn("I am not a number!");

    runnable.run();

    verify(tracker, never()).track(anyString(), any(Number.class), any(List.class));
    verify(handler).handleError(anyString(), any(Throwable.class));
  }

  @Test
  public void testJmxException() throws Exception {
    final MBeanException e = new MBeanException(new Exception(), "DIE!");
    doThrow(e).when(server).getAttribute(any(ObjectName.class), anyString());

    runnable.run();

    verify(tracker, never()).track(anyString(), any(Number.class), any(List.class));
    verify(handler).handleError(anyString(), eq(e));
  }

  @Test
  public void testTrackerException() throws Exception {
    final RuntimeException e = new RuntimeException("DIE!");
    when(server.getAttribute(new ObjectName(objectName), attribute)).thenReturn(100);
    doThrow(e).when(tracker).track(anyString(), any(Number.class), any(List.class));

    runnable.run();

    verify(tracker).track("metric", 100, dimensions);
    verify(handler).handleError(anyString(), eq(e));
  }
}
