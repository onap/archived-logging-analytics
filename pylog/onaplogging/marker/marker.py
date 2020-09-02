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

from typing import Iterable, List, Optional, Union, Iterator
from deprecated import deprecated
from warnings import warn
from logging import LogRecord

MARKER_TAG = "marker"


class Marker(object):
    """Abstract class for defining the marker structure.

    TODO:
        after deprecated child methods are removed, rename them here.
    Extends:
        object
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

    It is a marker with base functionalities that add sub-level markers and
    check  if another marker  exists as the parent itself  or as its  child.

    Extends:
        Marker
    Properties:
        name            : The name of the marker.
        children (list) : The list of all children (sub-level) markers.
    Arguments:
        name (str)      : The name of the marker.
    Methods:
        getName         : Returns the name of the marker.
        addChild        : Adds a sub-level marker.
        addChilds       : Adds a list of sub-level markers.
        removeChild     : Removes a specified sub-level marker.
        contains        : Checks if a sub-level marker exists.
    """

    @property
    def name(self):
        # type: () -> str
        """Name of the parent marker."""
        return self.__name

    @property
    def children(self):
        # type: () -> List[Marker]
        """Child markers of one parent marker."""
        return self.__childs

    @name.setter
    def name(self, value):
        # type: (str) -> None
        self.__name = value

    @children.setter
    def children(self, value):
        # type: (List[Marker]) -> None
        self.__childs = value

    def __init__(self, name):  # type: (str)
        """
        Raises:
            TypeError   : If the `name` parameter  is  not  a string.
            ValueError  : If the `name` parameter is an empty string.
        """

        super(BaseMarker, self).__init__()

        if not isinstance(name, str):
            raise TypeError("not str type")

        if name == "":
            raise ValueError("empty value")

        warn("Attribute `__childs` is replaced by the property `children`."
            "Use children instead.", DeprecationWarning)

        self.__name = name
        self.__childs = []

    def add_child(self, marker):
        # type: (Marker) -> None
        """Append a marker to child markers.

        Use this method to describe a different level of logs. For example,
        error  log  would use the  ERROR marker.  However it's  possible to
        create a,  for instance,  TYPE_ERROR  to mark  type related events.
        In this case TYPE_ERROR will be a child of parent ERROR.

        Args:
            marker      : marker describing a different log level.
        Raises:
            TypeError   : if the marker object has different type.
        """

        if not isinstance(marker, Marker):
            raise TypeError("Bad marker type.                     \
                             Can only add markers of type Marker. \
                             Type %s was passed." % type(marker))

        if self == marker:
            return

        if marker not in self.children:
            self.children.append(marker)

    def add_children(self, markers):
        # type: (Iterable[List]) -> None
        """ Append a list of markers to child markers.

        Args:
            markers     : An iterable object, containing markers.
        Raises:
            Exception   : If  `marker` parameter is not iterable.
        """

        try:
            iter(markers)
        except Exception as e:
            raise e

        for marker in markers:
            self.children.append(marker)

    def remove_child(self, marker):
        # type: (Marker) -> None
        """Use this method to remove a marker from the children list.

        Args:
            marker   : A child marker object.
        Raises:
            TypeError: if the marker object has different type.
        """

        if not isinstance(marker, Marker):
            raise TypeError("Bad marker type.                     \
                             Can only add markers of type Marker. \
                             Type %s was passed." % type(marker))

        if marker in self.children:
            self.children.remove(marker)

    def contains(self, item=None):
        # type: (Optional[Union[Marker, str]]) -> bool
        """
        Use it to check if a marker exists as a parent itself or its chidren.

        Args:
            item    : A child marker object. Defaults to None.
        Returns:
            bool    : True if the marker exists.
        """

        warn("`item` argument will be replaced with `marker`. "
             "Default value None will be removed.",
              DeprecationWarning)
        marker = item

        if isinstance(marker, Marker):
            if marker == self:
                return True
            return len(list(filter(
                lambda x: x == marker, self.children))) > 0

        elif isinstance(marker, str):
            if marker == self.name:
                return True

            return len(list(filter(
                lambda x: x.name == marker, self.children))) > 0

        return False

    def __iter__(self):
        # type: () -> Iterator[List[Marker]]
        return iter(self.__childs)

    def __hash__(self):
        # type (): -> int
        return hash(self.__name)

    def __eq__(self, other):
        # type: (Marker) -> bool
        if not isinstance(other, Marker):
            return False
        if id(self) == id(other):
            return True

        return self.__name == other.getName()

    @deprecated(reason="Will be removed. Call the `name` property instead.")
    def getName(self):
        """Class attribute getter."""
        return self.name

    @deprecated(reason="Will be removed. Call add_children(markers) instead.")
    def addChilds(self, childs):
        """Add a list of sub-level markers. See add_children(markers)"""
        self.add_children(childs)

    @deprecated(reason="Will be removed. Call add_child(marker) instead.")
    def addChild(self, item):
        """Add a sub-level marker. See add_child(marker)"""
        self.add_child(item)

    @deprecated(reason="Will be removed. Call remove_child(marker) instead.")
    def removeChild(self, item):
        """Remove a sub-level marker. See remove_child(marker)"""
        self.remove_child(item)


@deprecated(reason="Will be removed. "
    "Call match_marker(record, marker_to_match) instead.")
def matchMarkerHelp(record, markerToMatch):
    """See match_marker(record, marker_to_match)."""
    return match_markers(record, markerToMatch)


def match_markers(record, marker_to_match):
    # type: (LogRecord, Union[Marker, List]) -> bool
    """
    Use this method to match a marker (or a list of markers) with a LogRecord
    record.

    Args:
        record          : a record that may contain a marker.
        markerToMatch   : a marker or a list of markers.
    Raises:
        Exception       : if match check went wrong.
    Returns:
        bool            : whether the check can be done or the marker is found.
    """
    record_marker = getattr(record, MARKER_TAG, None)

    if record_marker is None or \
       marker_to_match is None:
        return False

    if not isinstance(record_marker, Marker):
        return False

    try:
        if isinstance(marker_to_match, list):
            return len(list(filter(
                lambda x: record_marker.contains(x), marker_to_match))) > 0

        return record_marker.contains(marker_to_match)
    except Exception as e:
        raise e
