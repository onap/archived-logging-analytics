# Copyright 2018 ke liang <lokyse@163.com>.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import logging
import threading
import io
import os
import traceback
import sys
import functools

from deprecated import deprecated
from typing import Dict, Optional, Any, Callable, List, Tuple
from logging import LogRecord

from onaplogging.utils.system import is_above_python_3_2

from .marker import Marker, MARKER_TAG

# TODO change to patch_logging_mdc after deprecated method is removed
__all__ = ['patch_loggingMDC', 'MDC']

_replace_func_name = ['info', 'critical', 'fatal', 'debug',
                      'error', 'warn', 'warning', 'log',
                      'handle', 'findCaller']


def fetchkeys(func):  # type: Callable[[str, List, Dict], None]
    # type: (...) -> Callable[[str, List, Dict], None]
    """MDC decorator.

    Fetchs contextual information from a logging call.
    Wraps by adding MDC to the `extra` field. Executes
    the call with  the updated contextual  information.
    """

    @functools.wraps(func)
    def replace(*args, **kwargs):
        # type: () -> None
        kwargs['extra'] = _getmdcs(extra=kwargs.get('extra', None))
        func(*args, **kwargs)

    return replace


class MDCContext(threading.local):
    """A Thread local instance that stores MDC values.

    Is initializ with an empty dictionary. Manages that
    dictionary to created Mapped Diagnostic Context.

    Extends:
        threading.local
    Property:
        local_dict  : a placeholder for MDC keys and values.
    """

    @property
    def local_dict(self):
        # type: () -> Dict
        return self._local_dict

    @local_dict.setter
    def local_dict(self, value):
        # type: (Dict) -> None
        self._local_dict = value

    def __init__(self):
        super(MDCContext, self).__init__()
        self.local_dict = {}

    def get(self, key):
        # type: (str) -> Any
        """Retrieve a value by key."""
        return self.local_dict.get(key, None)

    def put(self, key, value):
        # type: (str, Any) -> None
        """Insert or update a value by key."""
        self.local_dict[key] = value

    def remove(self, key):
        # type: (str) -> None
        """Remove a value by key, if exists."""
        if key in self.local_dict:
            del self.local_dict[key]

    def clear(self):
        # type: () -> None
        """Empty the MDC dictionary."""
        self.local_dict.clear()

    @deprecated(reason="Use local_mdc property instead.")
    def result(self):
        """Getter for the MDC dictionary."""
        return self.local_dict

    def empty(self):
        # type: () -> bool
        """Checks whether the local dictionary is empty."""
        return self.local_dict == {} or \
               self.local_dict is None

    @deprecated(reason="Will be replaced. Use empty() instead.")
    def isEmpty(self):
        """See empty()."""
        return self.empty()


MDC = MDCContext()


def _getmdcs(extra=None):
    # type: (Optional[Dict]) -> Dict
    """
    Puts an MDC dict in the `extra` field with key 'mdc'. This provides
    the contextual information with MDC.

    Args:
        extra       : Contextual information.       Defaults to None.
    Raises:
        KeyError    : a key from extra is attempted to be overwritten.
    Returns:
        dict        : contextual information named `extra` with MDC.
    """
    if MDC.empty():
        return extra

    mdc = MDC.local_dict

    if extra is not None:
        for key in extra:
            if  key in mdc or \
                key == 'mdc':
                raise KeyError("Attempt to overwrite %r in MDC" % key)
    else:
        extra = {}

    extra['mdc'] = mdc
    del mdc

    return extra


@fetchkeys
def info(self, msg, *args, **kwargs):
    # type: (str) -> None
    """If INFO enabled, deletage an info call with MDC."""
    if self.isEnabledFor(logging.INFO):
        self._log(logging.INFO, msg, args, **kwargs)


@fetchkeys
def debug(self, msg, *args, **kwargs):
    # type: (str) -> None
    """If DEBUG enabled, deletage a debug call with MDC."""
    if self.isEnabledFor(logging.DEBUG):
        self._log(logging.DEBUG, msg, args, **kwargs)


@fetchkeys
def warning(self, msg, *args, **kwargs):
    # type: (str) -> None
    """If WARNING enabled, deletage a warning call with MDC."""
    if self.isEnabledFor(logging.WARNING):
        self._log(logging.WARNING, msg, args, **kwargs)


@fetchkeys
def exception(self, msg, *args, **kwargs):
    # type: (str) -> None
    """Deletage an exception call and set exc_info code to 1."""
    kwargs['exc_info'] = 1
    self.error(msg, *args, **kwargs)


@fetchkeys
def critical(self, msg, *args, **kwargs):
    # type: (str) -> None
    """If CRITICAL enabled, deletage a critical call with MDC."""
    if self.isEnabledFor(logging.CRITICAL):
        self._log(logging.CRITICAL, msg, args, **kwargs)


@fetchkeys
def error(self, msg, *args, **kwargs):
    # type: (str) -> None
    """If ERROR enabled, deletage an error call with MDC."""
    if self.isEnabledFor(logging.ERROR):
        self._log(logging.ERROR, msg, args, **kwargs)


@fetchkeys
def log(self, level, msg, *args, **kwargs):
    # type: (int, str) -> None
    """
    If a specific logging level enabled and the code is represented
    as an integer value, delegate the call to the underlying logger.

    Raises:
        TypeError: if the logging level code is not an integer.
    """

    if not isinstance(level, int):
        if logging.raiseExceptions:
            raise TypeError("Logging level code must be an integer."
                            "Got %s instead." % type(level))
        else:
            return

    if self.isEnabledFor(level):
        self._log(level, msg, args, **kwargs)


def handle(self, record):
    # type: (LogRecord) -> None
    cmarker = getattr(self, MARKER_TAG, None)

    if isinstance(cmarker, Marker):
        setattr(record, MARKER_TAG, cmarker)

    if not self.disabled and \
       self.filter(record):
        self.callHandlers(record)


def findCaller(self, stack_info=False):
    # type: (bool) -> Tuple
    """
    Find the stack frame of the caller so that we can note the source file
    name, line number and function name. Enhances the logging.findCaller().
    """

    frame = logging.currentframe()

    if frame is not None:
        frame = frame.f_back
    rv = "(unkown file)", 0, "(unknow function)"

    while hasattr(frame, "f_code"):
        co = frame.f_code
        filename = os.path.normcase(co.co_filename)
        # jump through local 'replace' func frame
        if  filename == logging._srcfile or \
            co.co_name == "replace":

            frame = frame.f_back
            continue

        if is_above_python_3_2():

            sinfo = None
            if stack_info:

                sio = io.StringIO()
                sio.write("Stack (most recent call last):\n")
                traceback.print_stack(frame, file=sio)
                sinfo = sio.getvalue()

                if sinfo[-1] == '\n':
                    sinfo = sinfo[:-1]

                sio.close()
            rv = (co.co_filename, frame.f_lineno, co.co_name, sinfo)

        else:
            rv = (co.co_filename, frame.f_lineno, co.co_name)

        break

    return rv


def patch_logging_mdc():
    # type: () -> None
    """MDC patch.

    Sets MDC in a logging record instance at runtime.
    """
    localModule = sys.modules[__name__]

    for attr in dir(logging.Logger):
        if attr in _replace_func_name:
            newfunc = getattr(localModule, attr, None)
            if newfunc:
                setattr(logging.Logger, attr, newfunc)


@deprecated(reason="Will be removed. Call patch_logging_mdc() instead.")
def patch_loggingMDC():
    """See patch_logging_ymdc()."""
    patch_logging_mdc()
