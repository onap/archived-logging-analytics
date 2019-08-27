# Logging-Filter

The base project is meant to have very minimal dependencies for maximum re-use. The spring artifact is provided for any clients already using spring.

## Current Layout
- logging-filter (houses the parent pom)
  - **base** this artifact should only depend on javax.servlet-api, javax.ws.rs-api, slf4j-api and org.onap.logging-analytics:logging-slf4j
  - **spring** this artifact depends on base as well as spring.

## Design Principles
- minimize dependencies for maximum re-use
- code to APIs jaxrs vs jersey, slf4j vs logback, etc...
- no application specific code in this library
- write extensible code so applications can add on application specific code
- target Casablanca logging spec

## Code Formatting
ONAP code formatting standards are validated on builds. If you find a build failing due to a formatting issue, format your code by running `mvn process-sources -P format`