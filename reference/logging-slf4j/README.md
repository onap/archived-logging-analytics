# README - slf4j-reference

This project gives an example of ONAP-compliant logging using SLF4J logging.

## Adapter

In ```org.onap.logging.ref.slf4j```, there are TWO classes:
1. ```org.onap.logging.ref.slf4j.ONAPLogConstants```, providing declarations of standard ONAP Markers, MDCs and HTTP headers.
2. ```org.onap.logging.ref.slf4j.ONAPLogAdapter```, providing a lightweight, compliant implementation of the ONAP logging spec.

The adapter provides:
1. A loosely-coupled SLF4j logging wrapper:
 * To be used for logging ONAP ```entry```, ```exit``` and ```invoke``` behavior.
 * Devolving all *application* logging to the component, via the regular SLF4J ```Logger``` facade.
2. Customization options:
 * *Cheap*, by way of bean properties. This is suited to most Use Cases.
 * *Sophisticated*:
    * By OPTIONALLY implementing one of a number of adapters:
      * ```RequestAdapter``` to read incoming headers.
      * ```ServiceDescriptor``` for reporting attributes of the current service.
      * ```ResponseDescriptor``` for reporting outcomes.
      * ```RequestBuilder``` for setting southbound request headers.
    * By OPTIONALLY overriding methods like ```ONAPLogAdapter#setMDCs(RequestAdapter)```.

Note that:
* The adapter implementation uses static inner classes in order to fit in a single source file. This was an objective. 

## WAR

Building produces a simple (spring-boot](https://projects.spring.io/spring-boot/) example WAR, which can be launched from this directory with:

```bash
$ java -war target/*war
```

The example WAR in ```logging-slf4j-demo``` publishes four web services:
1. ```services/alpha```
2. ```services/beta```
3. ```services/gamma```
4. ```services/delta```

... each of which can invoke the others.

The purpose of the WAR is to demonstrate minimalist ONAP-compliant logging for web components, but a secondary purpose is to demonstrate that the call graph can be generated for a (mostly) representative set of interacting REST services.

## Tests

Tests for:
1. Code in the (potentially) reusable ``common`` package.
2. Validating that emitted logs can be used to generate an unambiguous call graph.
