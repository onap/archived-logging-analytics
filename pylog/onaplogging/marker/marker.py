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

MARKER_TAG = "marker"


class Marker(object):
    """Abstract class for defining the marker structure.

    Extends:
        object

    Metaclass:
        abc.ABCMeta

    Method list:
        getName
        addChild
        addChilds
        removeChild
        contains

    Raises:
        NotImplementedError
    """

    __metaclass__ = abc.ABCMeta

    @abc.abstractmethod
    def getName(self):
        raise NotImplementedError()

    @abc.abstractmethod
    def contains(self, item=None):
        raise NotImplementedError()

    @abc.abstractmethod
    def addChild(self, item):
        raise NotImplementedError()

    @abc.abstractmethod
    def removeChild(self, item):
        raise NotImplementedError()

    @abc.abstractmethod
    def __eq__(self, other):
        raise NotImplementedError()

    @abc.abstractmethod
    def __hash__(self):
        raise NotImplementedError()

    @abc.abstractmethod
    def __iter__(self):
        raise NotImplementedError()


class BaseMarker(Marker):
    """Basic marker class.

    It is a marker with base functionalities that add sub-level markers
    and check if another marker exists as the parent itself or as its child.

    Extends:
        Marker

    Attributes:
        __name (str): The name of the marker.
        __childs (list): The list of all children (sub-level) markers.

    Arguments:
        name (str): The name of the marker.

    Methods:
        getName: Returns the name of the marker.
        addChild: Adds a sub-level marker.
        addChilds: Adds a list of sub-level markers.
        removeChild: Removes a specified sub-level marker.
        contains: Checks if a sub-level marker exists.
    """

    def __init__(self, name):
        """
        Raises:
            TypeError: If the `name` parameter is not a string.
            ValueError: If the `name` parameter is an empty string.
        """

        super(BaseMarker, self).__init__()
        if not isinstance(name, str):
            raise TypeError("not str type")
        if name == "":
            raise ValueError("empty value")

        self.__name = name
        self.__childs = []

    def getName(self):
        """Class attribute getter.

        Returns:
            __name: The name of the marker.
        """
        return self.__name

    def contains(self, item=None):
        """Checker of the existance of a marker.

        Use it to check if a marker exists as a parent itself or its chidren.

        Args:
            item (Marker/str, optional): A `Marker` object or its name.
                                         Defaults to None.
        Returns:
            bool: True if the requested marker exists.
        """

        if isinstance(item, Marker):
            if item == self:
                return True
            return len(list(filter(
                lambda x: x == item, self.__childs))) > 0

        elif isinstance(item, str):
            if item == self.__name:
                return True

            return len(list(filter(
                lambda x: x.__name == item, self.__childs))) > 0

        return False

    def addChild(self, item):
        """Add a sub-level describing sub category.

        Use this method to describe a sublevel marker. For example, error
        logging would use the ERROR marker. However it's possible to create a,
        for instance, TYPE_ERROR to mark errors related to type errors.
        TYPE_ERROR will be a child of ERROR.

        Args:
            item (Marker): A sub-level `Marker` object.

        Raises:
            TypeError: If the type of `item` is not `Marker`.
        """

        if not isinstance(item, Marker):
            raise TypeError("can only add  (not %s) marker type"
                            % type(item))
        if self == item:
            return
        if item not in self.__childs:
            self.__childs.append(item)

    def addChilds(self, childs):
        """Add a list of sub-level markers.

        Use this method to add a list of sub-level (children) markers.

        Args:
            childs (list): A iterable object, containing markers.

        Raises:
            Exception: If `item` parameter is not iterable (e.g. not a list).
        """

        try:
            iter(childs)
        except Exception as e:
            raise e

        for item in childs:
            self.addChild(item)

    def removeChild(self, item):
        """Remove a sub-level marker.

        Use this method to remove a marker from the children list.

        Args:
            item (Marker): A `Marker` object.

        Raises:
            TypeError: If the type of `item` is not `Marker`.
        """

        if not isinstance(item, Marker):
            raise TypeError("can only add  (not %s) marker type"
                            % type(item))
        if item in self.__childs:
            self.__childs.remove(item)

    def __iter__(self):
        return iter(self.__childs)

    def __eq__(self, other):
        if not isinstance(other, Marker):
            return False
        if id(self) == id(other):
            return True

        return self.__name == other.getName()

    def __hash__(self):
        return hash(self.__name)


def matchMarkerHelp(record, markerToMatch):
    """Comparator as a helper method.

    Use this method to match objects: a marker extracted from the LogEvent
    record and a marker (or a list of markers).

    Args:
        record (LogEvent): A record that may contain a marker.
        markerToMatch (Marker/list): A marker or a list of markers.

    Raises:
        Exception: if match check went wrong.

    Returns:
        bool: Whether the check can be done or the marker is found.
    """

    marker = getattr(record, MARKER_TAG, None)

    if marker is None or markerToMatch is None:
        return False

    if not isinstance(marker, Marker):
        return False

    try:
        if isinstance(markerToMatch, list):
            return len(list(filter(
                lambda x: marker.contains(x), markerToMatch))) > 0

        return marker.contains(markerToMatch)
    except Exception as e:
        raise e
