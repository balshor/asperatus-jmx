package com.bizo.asperatus.jmx.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import org.junit.Test;

import static com.bizo.asperatus.model.Unit.*;

public class MetricConfigurationParserTest {

  private static final String testJsonLocation = "src/etc/test-resources/test-configuration.json";

  @Test
  public void testGet() throws Exception {
    BufferedReader reader = new BufferedReader(new FileReader(testJsonLocation));
    try {
      MetricConfigurationParser parser = new MetricConfigurationParser(reader);

      List<MetricConfiguration> configs = parser.get();

      assertNotNull(configs);
      assertEquals(5, configs.size());

      MetricConfiguration nonHeapConfig =
        new MetricConfiguration("java.lang:type=Memory", "NonHeapMemoryUsage", "NonHeapMemoryUsage", Count, 60, null);
      MetricConfiguration heapConfig =
        new MetricConfiguration("java.lang:type=Memory", "HeapMemoryUsage", "HeapMemoryUsage", Count, 60, null);
      MetricConfiguration fileDescriptorConfig =
        new MetricConfiguration(
          "java.lang:type=OperatingSystem",
          "OpenFileDescriptorCount",
          "OpenFileDescriptors",
          Count,
          60,
          "number of open file descriptors");
      MetricConfiguration systemLoadConfig =
        new MetricConfiguration(
          "java.lang:type=OperatingSystem",
          "SystemLoadAverage",
          "SystemLoadAverage",
          Count,
          300,
          "average system load");
      MetricConfiguration threadConfig =
        new MetricConfiguration(
          "java.lang:type=Threading",
          "ThreadCount",
          "ThreadCount",
          Count,
          60,
          "number of threads");

      assertTrue(configs.contains(nonHeapConfig));
      assertTrue(configs.contains(heapConfig));
      assertTrue(configs.contains(fileDescriptorConfig));
      assertTrue(configs.contains(systemLoadConfig));
      assertTrue(configs.contains(threadConfig));
    } finally {
      reader.close();
    }
  }
}
