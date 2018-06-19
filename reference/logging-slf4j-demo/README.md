# README - slf4j-reference

This project gives an example of ONAP-compliant logging using SLF4J logging.

Building produces a simple (spring-boot](https://projects.spring.io/spring-boot/) example WAR, which can be launched from this directory with:

```bash
$ java -war target/*war
```

The WAR publishes four web services:
1. ```services/alpha```
2. ```services/beta```
3. ```services/gamma```
4. ```services/delta```

... each of which can invoke the others.

The purpose of this WAR is to demonstrate minimalist ONAP-compliant logging for web components, but a secondary purpose is to demonstrate that the call graph can be generated for a (mostly) representative set of interacting REST services.

## Tests

Tests for verifying that emitted logs can be used to generate an unambiguous call graph.

Note that these run in-process, despite the WAR packaging. The intent is to enable tests via HTTP transports.
