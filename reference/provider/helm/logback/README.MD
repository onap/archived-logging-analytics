# Overview

This is an example of logback configuration templated for use with Helm.

It provides:

1. Boilerplate for configuring several appenders:
    1. Beats - machine-readable, tab-separated, fully escaped file.
    2. Console - somewhat human-readable (no tab or newline replacement in messages or stacktraces).
    3. EELF - legacy format, output sorted into several files.
    4. Syslog - Logstash-compatible network transport.
2. Values-based activation/deactivation of each appender.
3. Values-based configuration of root loglevels.
4. Values-based configuration of individual loggers and their loglevels.
5. Values-based configuration of queue lengths, rollover, reconfiguration intervals, etc.

# Installation

From ```./charts```, run:

```
helm install .
```

# Configuration

Via ```values.yaml```.

* ```componentName``` and ```subcomponentName``` always customized. These determine the output directory for files appenders. (And must therefore be unique per container, and also per deployment).
* Everything else customized only as necessary.

# Status

Before this can be made available generally available:

1. Discussion:
    1. Is this the right idea?
    2. Is this the right way to parameterize logging configuration?
    3. What can be done better?
        1. What should be parametrized by isn't?
        2. What is parameterized needlessly (i.e. providing a degree of configurability we don't want to support)?
2. Log4j (v1.X and v2.X) equivalents.
3. Test scenarios in this directory:
    1. Write some logs within the container.
    2. Parse the logs to verify that they're as expected.
    3. Run the same tests for each logging provider.

It doesn't:

1. Provide appender-specific logger-to-level mappings. Each logger gets the same level for each appender.
2. Define globals, or nececssarily fit into the nascent scheme for OOM helm chart parameterization.
