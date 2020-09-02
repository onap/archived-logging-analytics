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
    """Abstract marker factory for defining structure.

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

    Attributes:
        _instance (MarkerFactory): A marker factory instance.
        _marker_map (dict): The map of available markers.

    Methods:
        getMarker: Creates a new marker or returns an available one.
        deleteMarker: Removes a specific marker.
        exist: Checks if a specific marker exists.
    """

    _instance = None
    _marker_map = {}

    def __new__(cls, *args, **kwargs):

        if cls._instance is None:
            cls._instance = super(MarkerFactory, cls).__new__(cls)

        return cls._instance

    def getMarker(self, marker_name=None):
        """Marker getter/setter

        Use it to get any marker by its name. If it doesn't exist -
        it will create a new marker that will be added to the factory.

        Args:
            marker_name (str, optional): A marker name. Defaults to None.

        Raises:
            ValueError: If `marker_name` is None.

        Returns:
            Marker: A found or just created marker.
        """

        if marker_name is None:
            raise ValueError("the marker name should be specified")

        lock.acquire()
        marker = self._marker_map.get(marker_name, None)
        if marker is None:
            marker = BaseMarker(name=marker_name)
            self._marker_map[marker_name] = marker
        lock.release()

        return marker

    def deleteMarker(self, marker_name=None):
        """Removes a marker.

        Args:
            marker_name (str, optional): A marker name. Defaults to None.

        Returns:
            bool: The status of deletion.
        """

        lock.acquire()
        if self.exist(marker_name):
            del self._marker_map[marker_name]
            return True
        lock.release()
        return False

    def exist(self, marker_name=None):
        """Method checking the existance of a marker.

        Checks whether the search for a marker returns None and returns the
        status of the operation.

        Args:
            marker_name (str, optional): The name of the marker to be checked.
                                         Defaults to None.
        Returns:
            bool: Status of whether the marker was found.
        """

        return self._marker_map.get(
            marker_name, None) is not None
