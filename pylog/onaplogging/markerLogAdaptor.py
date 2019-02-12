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

    @wraps(func)
    def wrapper(self, marker, msg, *args, **kwargs):
        lock.acquire()
        if not isinstance(marker, Marker):
            raise TypeError("not marker type %s"
                            % type(marker))

        if self.extra and MARKER_TAG in self.extra:
            raise Exception("cann't  add 'marker' in extra")
        setattr(self.logger, MARKER_TAG, marker)
        func(self, marker, msg, *args, **kwargs)
        if hasattr(self.logger, MARKER_TAG):
            delattr(self.logger, MARKER_TAG)
        lock.release()
    return wrapper


class MarkerLogAdaptor(LoggerAdapter):

    def process(self, msg, kwargs):

        if sys.version_info > (3, 2):
            kwargs['extra'] = _getmdcs(self.extra)
        else:
            kwargs['extra'] = self.extra
        return msg, kwargs

    @addMarker
    def infoMarker(self, marker, msg, *args, **kwargs):

        self.info(msg, *args, **kwargs)

    @addMarker
    def debugMarker(self, marker, msg, *args, **kwargs):

        self.debug(msg, *args, **kwargs)

    @addMarker
    def warningMarker(self, marker, msg, *args, **kwargs):

        self.warning(msg, *args, **kwargs)

    @addMarker
    def errorMarker(self, marker, msg, *args, **kwargs):

        self.error(msg, *args, **kwargs)

    @addMarker
    def exceptionMarker(self, marker, msg, *arg, **kwargs):
        self.exception(msg, *arg, **kwargs)

    @addMarker
    def criticalMarker(self, marker, msg, *arg, **kwargs):
        self.critical(msg, *arg, **kwargs)

    @addMarker
    def logMarker(self, marker, level, msg, *arg, **kwargs):
        self.log(level, msg, *arg, **kwargs)
