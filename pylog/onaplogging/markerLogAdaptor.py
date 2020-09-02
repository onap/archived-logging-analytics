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

import sys
from logging import LoggerAdapter
from threading import RLock
from functools import wraps
from .marker import MARKER_TAG
from .marker import Marker
from .mdcContext import _getmdcs

lock = RLock()


def addMarker(func):
    """Marker decorator.

    Requests a blocking acquisition of the thread. Sets the marker
    as the logger's marker and delegates a call to the underlying
    logger with contextual information. Next it removes the marker
    and releases the thread.

    Args:
        func (method): a method supplied with a logging marker.

    Raises:
        TypeError: the marker type is not `Marker`.
        Exception: `extra` doesn't exist or MARKER_TAG is already in `extra`.

    Returns:
        method: decorated method.
    """
    @wraps(func)
    def wrapper(self, marker, msg, *args, **kwargs):
        lock.acquire()
        if not isinstance(marker, Marker):
            raise TypeError("%s should be of type Marker"
                            % type(marker))

        if self.extra and MARKER_TAG in self.extra:
            raise Exception("can't add 'marker' in extra - either extra \
                 exists or MARKER_TAG is alredy in extra")
        setattr(self.logger, MARKER_TAG, marker)
        func(self, marker, msg, *args, **kwargs)
        if hasattr(self.logger, MARKER_TAG):
            delattr(self.logger, MARKER_TAG)
        lock.release()
    return wrapper


class MarkerLogAdaptor(LoggerAdapter):
    """Contextual loggin adapter.

    Specifies contextual information in logging output.
    Takes a logger and a dictionary-like object `extra` for providing
    contextual information.

    An example of the extra contextual information:
    extra = {'app_name':'Marker Logging'}

    Extends:
        logging.LoggerAdapter
    """

    def process(self, msg, kwargs):
        """Logging call processor.

        Takes a logging message and keyword arguments to provide
        cotextual information.

        Args:
            msg (str): Logging information.
            kwargs (dict): Contextual information.

        Returns:
            str: Logging message.
            dict: modified (or not) contextual information.
        """
        if sys.version_info > (3, 2):
            kwargs['extra'] = _getmdcs(self.extra)
        else:
            kwargs['extra'] = self.extra
        return msg, kwargs

    @addMarker
    def infoMarker(self, marker, msg, *args, **kwargs):
        """Provide the logger with an informational call."""
        self.info(msg, *args, **kwargs)

    @addMarker
    def debugMarker(self, marker, msg, *args, **kwargs):
        """Provide the logger with a debug call."""
        self.debug(msg, *args, **kwargs)

    @addMarker
    def warningMarker(self, marker, msg, *args, **kwargs):
        """Provide the logger with a warning call."""
        self.warning(msg, *args, **kwargs)

    @addMarker
    def errorMarker(self, marker, msg, *args, **kwargs):
        """Provide the logger with an error call."""
        self.error(msg, *args, **kwargs)

    @addMarker
    def exceptionMarker(self, marker, msg, *arg, **kwargs):
        """Provide the logger with an exceptional call."""
        self.exception(msg, *arg, **kwargs)

    @addMarker
    def criticalMarker(self, marker, msg, *arg, **kwargs):
        """Provide the logger with a critical call."""
        self.critical(msg, *arg, **kwargs)

    @addMarker
    def logMarker(self, marker, level, msg, *arg, **kwargs):
        """Provide the logger with a log call."""
        self.log(level, msg, *arg, **kwargs)
