package com.bizo.asperatus.jmx.configuration;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.bizo.asperatus.model.Unit;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

public class MetricConfigurationParser implements Supplier<List<MetricConfiguration>> {
  private final Reader reader;

  public MetricConfigurationParser(final String json) {
    this(new StringReader(json)); // Note: string readers do not need to be closed, so no worries about ownership.
  }

  public MetricConfigurationParser(final Reader reader) {
    this.reader = reader;
  }

  @Override
  public List<MetricConfiguration> get() {
    final ImmutableList.Builder<MetricConfiguration> builder = ImmutableList.builder();
    final JSONParser parser = new JSONParser();
    try {
      final Object root = parser.parse(reader);
      if (!(root instanceof JSONArray)) {
        throw new MetricConfigurationException("Root object of a metric configuration must be a JSON Array");
      }
      for (final Object item : (JSONArray) root) {
        if (!(item instanceof JSONObject)) {
          throw new MetricConfigurationException("Items in the root JSON array must be objects");
        }
        builder.add(parse((JSONObject) item));
      }
    } catch (final ParseException pe) {
      throw new MetricConfigurationException(pe);
    } catch (final IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return builder.build();
  }

  private MetricConfiguration parse(final JSONObject json) {
    final String objectName = requiredString(json, "objectName");
    final String attribute = requiredString(json, "attribute");
    final String compositeDataKey;
    if (json.containsKey("compositeDataKey")) {
      compositeDataKey = requiredString(json, "compositeDataKey");
    } else {
      compositeDataKey = null;
    }
    final String metricName = requiredString(json, "metricName");
    final String unitString = requiredString(json, "unit");
    final Unit unit;
    try {
      unit = Unit.fromValue(unitString);
    } catch (final Exception e) {
      throw new MetricConfigurationException("Invalid unit " + unitString, e);
    }
    final int frequency;
    if (json.containsKey("frequency")) {
      final Object obj = json.get("frequency");
      if (obj instanceof Number) {
        frequency = ((Number) obj).intValue();
      } else {
        throw new MetricConfigurationException("Frequency had a non-numeric value " + obj.toString());
      }
    } else {
      frequency = 60;
    }
    final String comment;
    if (json.containsKey("comment")) {
      comment = requiredString(json, "comment");
    } else {
      comment = null;
    }
    return new MetricConfiguration(objectName, attribute, compositeDataKey, metricName, unit, frequency, comment);
  }

  private String requiredString(final JSONObject json, final String key) {
    final Object obj = json.get(key);
    if (obj != null && obj instanceof String) {
      return (String) obj;
    }
    throw new MetricConfigurationException("Configuration had a missing or non-string value for " + key);
  }

}
