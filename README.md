Asperatus JMX
=============

This library adds JMX support to the Asperatus metric tracking library.

Design
------

Each metric to be pulled from JMX and pushed into Asperatus is described in a `MetricConfiguration`.  `MetricConfiguration`s may be constructed programatically or read from JSON via a `MetricConfigurationParser`.

A `MetricConfiguration` is combined with the JMX `MBeanServer`, the Asperatus `MetricTracker`, and a list of the CloudWatch dimensions to create a `MetricRunnable`.
  
An `AsperatusJmxBridge` is responsible for scheduling these `MetricRunnable` instances on a `ScheduledExcecutorService`.

MetricConfiguration JSON
------------------------

The `MetricConfigurationParser` will deserialize one or more `MetricConfiguration`s from a JSON Array.  The `ClasspathConfigurationSupplier` is a utility class built around `MetricConfigurationParser` to simplify loading a JSON resource from the classpath.

    [
      {
        "objectName"       : "java.lang:type=OperatingSystem",
        "attribute"        : "OpenFileDescriptorCount",
        "metricName"       : "OpenFileDescriptors",
        "unit"             : "Count",
        "comment"          : "number of open file descriptors"
      },
      {
        "objectName"       : "java.lang:type=Memory",
        "attribute"        : "NonHeapMemoryUsage",
        "compositeDataKey" : "used",
        "metricName"       : "NonHeapMemoryUsage",
        "unit"             : "Count"
      }, ...
    ]

`objectName` corresponds to the `MBean`'s `ObjectName`.  (http://docs.oracle.com/javase/7/docs/api/javax/management/ObjectName.html)

`attribute` corresponds to the `MBeanAttributeInfo` that should be retrieved.  This attribute must either be numeric or composite.  (http://docs.oracle.com/javase/7/docs/api/javax/management/MBeanAttributeInfo.html)

If the requested MBean is composite (http://docs.oracle.com/javase/7/docs/api/javax/management/openmbean/CompositeData.html), the `compositeDataKey` specifies which key to retrieve.  The corresponding value must be numeric.

`metricName` is the Asperatus/Cloudwatch metric name.

`unit` is the Asperatus/Cloudwatch unit.

`comment` is ignored by this library.

See src/test/java/com/bizo/asperatus/jmx/configuration/test-configuration.json for more examples.

Sample Usage
------------

    // on startup
    MetricTracker tracker = ... // from Asperatus
    AsperatusJmxBridge bridge = new AsperatusJmxBridge(tracker);
    String classpathConfigLocation = "com/bizo/asperatus/jmx/configuration/test-configuration.json";
    ClasspathConfigurationSupplier supplier = new ClasspathConfigurationSupplier(classpathConfigLocation);
    List<MetricConfiguration> configurations = supplier.get();
    bridge.monitor(configurations); // may be called again with new configurations if necessary

    // on shutdown    
    bridge.shutdown();

Dependencies
------------

Dependencies are currently managed via Ivy; however, the configuration there has not yet been ported from Bizo's internal repository to a public repository.  For convenience, the required dependencies are listed here:

* Apache Commons-Lang 2.6 (http://commons.apache.org/lang/)
* Bizo Asperatus (https://github.com/ogrodnek/asperatus)
* Google Guava 12.0.1 (http://code.google.com/p/guava-libraries/)
* json-simple 1.1 (http://code.google.com/p/json-simple/)
