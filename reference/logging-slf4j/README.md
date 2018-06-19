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

The purpose of the WAR is to demonstrate minimalist ONAP-compliant logging for web components, but a secondary purpose is to demonstrate that the call graph can be generated for a (mostly) representative set of interacting REST services.

## Configuration

Note that the bundled SLF4J configuration *isn't* meant to be normative. Close enough as makes no difference for
the purposes of tests, but be aware that it might not always be up-to-date.

## Tests

TestNG tests for contract + output of SLF4J reference impl.
