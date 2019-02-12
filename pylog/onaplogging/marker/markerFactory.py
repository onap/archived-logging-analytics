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

import abc
import threading
from .marker import BaseMarker

lock = threading.RLock()


class IMarkerFactory(object):
    __metaclass__ = abc.ABCMeta

    @abc.abstractmethod
    def getMarker(self, marker_name=None):
        raise NotImplementedError()

    @abc.abstractmethod
    def deleteMarker(self, marker_name=None):
        raise NotImplementedError()

    @abc.abstractmethod
    def exist(self, marker_name=None):
        raise NotImplementedError()


class MarkerFactory(IMarkerFactory):

    _instance = None
    _marker_map = {}

    def __new__(cls, *args, **kwargs):

        if cls._instance is None:
            cls._instance = super(MarkerFactory, cls).__new__(cls)

        return cls._instance

    def getMarker(self, marker_name=None):
        if marker_name is None:
            raise ValueError("not empty")

        lock.acquire()
        marker = self._marker_map.get(marker_name, None)
        if marker is None:
            marker = BaseMarker(name=marker_name)
            self._marker_map[marker_name] = marker
        lock.release()

        return marker

    def deleteMarker(self, marker_name=None):
        lock.acquire()
        if self.exist(marker_name):
            del self._marker_map[marker_name]
            return True
        lock.release()
        return False

    def exist(self, marker_name=None):

        return self._marker_map.get(
            marker_name, None) is not None
