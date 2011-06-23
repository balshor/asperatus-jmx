package com.bizo.asperatus.jmx.configuration;

import java.io.*;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

/**
 * This class simply supplies a static configuration loaded from the classpath. It is intended to simplify configuration
 * of the bridge in Spring-based applications where managing input streams is difficult to express.
 * 
 * @author darren
 * 
 */
public class ClasspathConfigurationSupplier implements Supplier<List<MetricConfiguration>> {
  private final List<MetricConfiguration> defaultConfiguration;

  public ClasspathConfigurationSupplier(final String classpathLocation) {
    final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathLocation);
    if (is == null) {
      throw new IllegalArgumentException(String.format(
        "Could not find resource [%s] on the classpath.",
        classpathLocation));
    }
    final Reader r = new BufferedReader(new InputStreamReader(is));
    try {
      final MetricConfigurationParser parser = new MetricConfigurationParser(r);
      defaultConfiguration = parser.get();
    } finally {
      try {
        r.close();
      } catch (final IOException e) {
        // ignore
      }
    }
  }

  @Override
  public List<MetricConfiguration> get() {
    return ImmutableList.copyOf(defaultConfiguration);
  }
}
