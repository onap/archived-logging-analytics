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

from deprecated import deprecated
from warnings import warn
from typing import Dict, Optional

from .marker import Marker
from .marker import BaseMarker

lock = threading.RLock()


class IMarkerFactory(object):
    """Abstract marker factory for defining structure.

    TODO:
        after deprecated child methods are removed, rename them here.
    Extends:
        object
    Method list:
        getMarker
        deleteMarker
        exist
    Raises:
        NotImplementedError
    """

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
    """A factory class maganing every marker.

    It is designed to check the existance, create and remove single markers.
    This class follows a singleton pattern - only one instance can be created.

    Extends:
        IMarkerFactory
    Properties:
        marker_map      : a map of existing markers.
    Attributes:
        _instance       : a marker factory instance.
    Methods:
        getMarker       : creates a new marker or returns an available one.
        deleteMarker    : removes a specific marker.
        exist           : checks if a specific marker exists.
    """

    _instance = None
    _marker_map = {}

    @property
    def marker_map(self):
        # type: () -> Dict
        if not hasattr(self, '_marker_map'):
            self._marker_map = {}
        return self._marker_map

    def get_marker(self, name=None):
        # type: (Optional[str]) -> Marker
        """
        Use it to get any marker by its name. If it doesn't exist - it
        will  create a new  marker that  will be added to the factory.
        Blocks the thread while executing.

        Args:
            name        : A marker name. Defaults to None.
        Raises:
            ValueError  : If `name` is None.
        Returns:
            Marker      : A found or just newly created marker.
        """

        if name is None:
            raise ValueError("Marker name is None. Must have a str value.")

        lock.acquire()

        marker = self.marker_map.get(name, None)

        if marker is None:
            marker = BaseMarker(name)
            self.marker_map[name] = marker

        lock.release()

        return marker

    def delete_marker(self, name=None):
        # type: (Optional[str]) -> bool
        """
        Args:
            name: A marker name. Defaults to None.
        Returns:
            bool: The status of deletion.
        """

        lock.acquire()
        exists = self.exists(name)
        if exists:
            del self.marker_map[name]
            return True
        lock.release()

        return False

    def exists(self, name=None):
        # type: (Optional[str]) -> bool
        """
        Checks whether the search for a marker returns None and returns the
        status of the operation.

        Args:
            name: marker name. Defaults to None.
        Returns:
            bool: status of whether the marker was found.
        """
        marker = self.marker_map.get(name, None)
        return marker is not None

    def __new__(cls, *args, **kwargs):
        if cls._instance is None:
            cls._instance = super(MarkerFactory, cls).__new__(cls)

        warn("_marker_map attribute will be replaced by marker_map property.",
              DeprecationWarning)
        return cls._instance

    @deprecated(reason="Will be removed. Call exists(name) instead.")
    def exist(self, marker_name=None):
        return self.exists(marker_name)

    @deprecated(reason="Will be removed. Call get_marker(name) instead.")
    def getMarker(self, marker_name=None):
        return self.get_marker(marker_name)

    @deprecated(reason="Will be removed. Call delete_marker(name) instead.")
    def deleteMarker(self, marker_name=None):
        return self.delete_marker(marker_name)
