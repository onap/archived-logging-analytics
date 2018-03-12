# ONAP python logging package
- python-package onappylog extend python standard logging library which
could be used in any python project to log MDC(Mapped Diagnostic Contex)
and easy to reload logging at runtime.

-----

## install package
```bash
  pip install onappylog
```

## Usage

### 1. MDC monkey patch

Import the MDC monkey patch making logRecord to store context in local thread.

```python
   from onaplogging import monkey; monkey.patch_loggingMDC()
```
Import the MDC format to be used to configure mdc output format.
Please replace your old logging format with mdc format in configuration.


```python
  from onaplogging import mdcformatter
```
the mdc format  example
```python
'mdcFormater':{
          '()': mdcformatter.MDCFormatter, # Use MDCFormatter instance to convert logging string
          'format': '%(mdc)s and other %-style key ', #  Add '%(mdc)s' here.
          'mdcfmt':  '{key1} {key2}', # Define your mdc keys here.
          'datefmt': '%Y-%m-%d %H:%M:%S'  # date format
      }
```

Import MDC to store context in python file with logger
code.

```python
from onaplogging.mdcContext import MDC
# add mdc  
MDC.put("key1", "value1")
MDC.put("key2", "value2")

# origin code
logger.info("msg")
logger.debug("debug")

```

### 2. Reload logging at runtime

It's thread safe to reload logging. If you want to use this feature,
must use yaml file to configure logging.


import the  yaml monkey patch and load logging yaml file

```python
  from onaplogging import monkey,monkey.patch_loggingYaml()
  # yaml config
  config.yamlConfig(filepath=<yaml filepath>, watchDog=True)
```

Notice that the watchDog is opening,So your logging could be reloaded at runtime.
if you modify yaml file to change handler、filter or format,
the logger in program will be reloaded to use new configuration.

Set watchDog to **false**, If you don't need to reloaded logging.




Yaml configure exmaple

```yaml
version: 1

disable_existing_loggers: True

loggers:
vio:
    level: DEBUG
    handlers: [vioHandler]
    propagate: False
handlers:
vioHandler:
    class: logging.handlers.RotatingFileHandler
    level: DEBUG
    filename: /var/log/bt.log
    mode: a
    maxBytes: 1024*1024*50
    backupCount: 10
    formatter: mdcFormatter
formatters:
  mdcFormatter:
    format: "%(asctime)s:[%(name)s] %(created)f %(module)s %(funcName)s %(pathname)s %(process)d %(levelno)s :[ %(threadName)s  %(thread)d]: [%(mdc)s]: [%(filename)s]-[%(lineno)d] [%(levelname)s]:%(message)s"
    mdcfmt: "{key1} {key2} {key3} {key4} dwdawdwa "
    datefmt: "%Y-%m-%d %H:%M:%S"
    (): onaplogging.mdcformatter.MDCFormatter
  standard:
    format: '%(asctime)s:[%(name)s]:[%(filename)s]-[%(lineno)d]
        [%(levelname)s]:%(message)s  '
    datefmt: "%Y-%m-%d %H:%M:%S"

```


### 3. reference

[What's MDC?](https://logging.apache.org/log4j/2.x/manual/thread-context.html)

[Onap Logging Guidelines](https://wiki.onap.org/pages/viewpage.action?pageId=20087036)

[Python Standard Logging Library](https://docs.python.org/2/library/logging.html)
