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

from logging import LoggerAdapter
from threading import RLock
from functools import wraps
from deprecated import deprecated
from typing import Dict, Callable

from onaplogging.utils.system import is_above_python_3_2

from .marker import Marker, MARKER_TAG
from .mdcContext import _getmdcs

lock = RLock()


def add_marker(func):
    # type: ( Callable[[Marker, str], None] ) -> Callable[[Marker, str], None]
    """Marker decorator.

    Requests a blocking acquisition of the thread. Sets the marker
    as the logger's marker and delegates a call to the underlying
    logger with contextual information. Next it removes the marker
    and releases the thread.

    Args:
        func        : a method supplied with a logging marker.
    Raises:
        TypeError   : the marker type is not `Marker`.
        Exception   : `extra` doesn't exist or MARKER_TAG is in `extra`.

    Returns:
        method: decorated method.
    """
    @wraps(func)
    def wrapper(self, marker, msg, *args, **kwargs):
        # type: (Marker, str) -> Callable[[Marker, str], None]

        lock.acquire()

        if not isinstance(marker, Marker):
            raise TypeError("Passed a marker of type %s. \
                             Should have the type %s."
                            % type(marker), "Marker")

        if  self.extra and \
            MARKER_TAG in self.extra:
            raise Exception("Can't add 'marker' in extra - either extra \
                             exists or MARKER_TAG is alredy in extra")

        setattr(self.logger, MARKER_TAG, marker)

        func(self, marker, msg, *args, **kwargs)

        if hasattr(self.logger, MARKER_TAG):
            delattr(self.logger, MARKER_TAG)

        lock.release()

    return wrapper


@deprecated(reason="@addMarker is deprecated. Use @add_marker instead.")
def addMarker(func):
    """Decorator. See new decorator add_marker(func)."""
    add_marker(func)


class MarkerLogAdaptor(LoggerAdapter):
    """Contextual loggin adapter.

    Specifies contextual information in logging output. Takes a logger and a
    dictionary-like object `extra` for providing contextual information.

    An example of the extra contextual information:
    extra = {'app_name':'Marker Logging'}

    Extends:
        logging.LoggerAdapter
    """

    def process(self, msg, kwargs):
        # type: (str, Dict)
        """Logging call processor.

        Takes a logging message and keyword arguments to provide cotextual
        information.

        Args:
            msg     : Logging information.
            kwargs  : Contextual information.
        Returns:
            str     : Logging message.
            dict    : modified (or not) contextual information.
        """
        if is_above_python_3_2():
            kwargs['extra'] = _getmdcs(self.extra)
        else:
            kwargs['extra'] = self.extra
        return msg, kwargs

    @add_marker
    def info_marker(self, marker, msg, *args, **kwargs):
        # type: (Marker, str) -> None
        """Provide the logger with an informational call."""
        self.info(msg, *args, **kwargs)

    @add_marker
    def debug_marker(self, marker, msg, *args, **kwargs):
        # type: (Marker, str) -> None
        """Provide the logger with a debug call."""
        self.debug(msg, *args, **kwargs)

    @add_marker
    def warning_marker(self, marker, msg, *args, **kwargs):
        # type: (Marker, str) -> None
        """Provide the logger with a warning call."""
        self.warning(msg, *args, **kwargs)

    @add_marker
    def error_marker(self, marker, msg, *args, **kwargs):
        # type: (Marker, str) -> None
        """Provide the logger with an error call."""
        self.error(msg, *args, **kwargs)

    @add_marker
    def exception_marker(self, marker, msg, *args, **kwargs):
        # type: (Marker, str) -> None
        """Provide the logger with an exceptional call."""
        self.exception(msg, *args, **kwargs)

    @add_marker
    def critical_marker(self, marker, msg, *args, **kwargs):
        # type: (Marker, str) -> None
        """Provide the logger with a critical call."""
        self.critical(msg, *args, **kwargs)

    @add_marker
    def log_marker(self, marker, level, msg, *args, **kwargs):
        # type: (Marker, str) -> None
        """Provide the logger with a log call."""
        self.log(marker, level, msg, *args, **kwargs)

    @deprecated(reason="infoMarker(...) is replaced with info_marker(...).")
    def infoMarker(self, marker, msg, *args, **kwargs):
        self.info_marker(marker, msg, *args, **kwargs)

    @deprecated(reason="debugMarker(...) is replaced with debug_marker(...).")
    def debugMarker(self, marker, msg, *args, **kwargs):
        self.debug_marker(marker, msg, *args, **kwargs)

    @deprecated(reason="warningMarker(...) replaced, use warning_marker(...).")
    def warningMarker(self, marker, msg, *args, **kwargs):
        self.warning_marker(marker, msg, *args, **kwargs)

    @deprecated(reason="errorMarker(...) is replaced with error_marker(...).")
    def errorMarker(self, marker, msg, *args, **kwargs):
        self.error_marker(marker, msg, *args, **kwargs)

    @deprecated(reason="exceptionMarker(...) replaced,"
        " use exception_marker(...).")
    def exceptionMarker(self, marker, msg, *args, **kwargs):
        self.exception_marker(marker, msg, *args, **kwargs)

    @deprecated(reason="criticalMarker(...) is replaced, "
        "use critical_marker(...).")
    def criticalMarker(self, marker, msg, *args, **kwargs):
        self.critical_marker(marker, msg, *args, **kwargs)

    @deprecated(reason="logMarker(...) is replaced with info_marker(...).")
    def logMarker(self, marker, level, msg, *args, **kwargs):
        self.log_marker(marker, level, msg, *args, **kwargs)
