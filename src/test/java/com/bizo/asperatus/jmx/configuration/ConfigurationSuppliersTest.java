package com.bizo.asperatus.jmx.configuration;

import static com.bizo.asperatus.model.Unit.BytesSecond;
import static com.bizo.asperatus.model.Unit.Count;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import org.junit.Test;

public class ConfigurationSuppliersTest {

  private static final String nonClasspathConfigLocation = "src/etc/test-resources/test-configuration.json";
  private static final String classpathConfigLocation = "com/bizo/asperatus/jmx/configuration/test-configuration.json";

  @Test
  public void testClasspathConfigurationSupplier() throws Exception {
    ClasspathConfigurationSupplier supplier = new ClasspathConfigurationSupplier(classpathConfigLocation);
    assertConfigurations(supplier.get());
  }

  @Test(expected = RuntimeException.class)
  public void testNonExistantConfigurationSupplier() throws Exception {
    new ClasspathConfigurationSupplier(classpathConfigLocation + ".dne");
  }

  @Test
  public void testParser() throws Exception {
    BufferedReader reader = new BufferedReader(new FileReader(nonClasspathConfigLocation));
    try {
      MetricConfigurationParser parser = new MetricConfigurationParser(reader);
      assertConfigurations(parser.get());
    } finally {
      reader.close();
    }
  }

  private static void assertConfigurations(List<MetricConfiguration> configs) {
    assertNotNull(configs);
    assertEquals(6, configs.size());

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
      new MetricConfiguration("java.lang:type=Threading", "ThreadCount", "ThreadCount", Count, 60, "number of threads");
    MetricConfiguration fakeMeterConfig =
      new MetricConfiguration(
        "com.bizo:type=FakeMeter",
        "Speed",
        "FakeMeterSpeed",
        BytesSecond,
        60,
        "this is a made-up metric for testing");

    assertTrue(configs.contains(nonHeapConfig));
    assertTrue(configs.contains(heapConfig));
    assertTrue(configs.contains(fileDescriptorConfig));
    assertTrue(configs.contains(systemLoadConfig));
    assertTrue(configs.contains(threadConfig));
    assertTrue(configs.contains(fakeMeterConfig));

  }
}
