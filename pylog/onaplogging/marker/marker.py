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
        raise  NotImplementedError()

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

    def __init__(self, name):
        super(BaseMarker,self).__init__()
        if not isinstance(name, str):
            raise TypeError("not str type")
        if name == "":
            raise ValueError("empty value")
        self.__name = name
        self.__childs = []

    def getName(self):
        return self.__name

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

    def contains(self, item = None):

        if isinstance(item, Marker):
            if item == self:
                return True
            return len(list(filter(
                lambda x: x == item, self.__childs))) > 0

        elif isinstance(item ,str):
            if item == self.__name:
                return True

            return len(list(filter(
                lambda x: x.__name == item, self.__childs))) > 0

        return False


    def addChild(self, item):
        if not isinstance(item, Marker):
            raise TypeError("can only add  (not %s) marker type"
                            % type(item))
        if self == item:
            return
        if item not in self.__childs:
            self.__childs.append(item)

    def addChilds(self, childs):
        try:
            iter(childs)
        except Exception as e:
            raise e

        for item in childs:
            self.addChild(item)

    def removeChild(self, item):
        if not isinstance(item, Marker):
            raise TypeError("can only add  (not %s) marker type"
                            % type(item))
        if item in self.__childs:
            self.__childs.remove(item)


def matchMarkerHelp(record, markerToMatch):

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
