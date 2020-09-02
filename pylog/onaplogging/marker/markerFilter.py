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

from logging import Filter, LogRecord
from warnings import warn
from typing import List, Optional, Union

from onaplogging.utils.system import is_above_python_2_7

from .marker import match_markers, Marker


class MarkerFilter(Filter):
    """Marker filtering.

    Extends:
        logging.Filter
    Properties:
        marker_to_match (Marker/list): a marker of list of markers.
    Methods
        filter: Filter records by the current filter marker(s).
    """

    @property
    def markers_to_match(self):
        # type: () -> Union[Marker, List[Marker]]
        return self.markersToMatch  # TODO renamed - deprecated

    @markers_to_match.setter
    def markers_to_match(self, value):
        # type: ( Union[Marker, List[Marker]] ) -> None
        self.markersToMatch = value

    def __init__(self,
                 name="",        # type: str
                 markers=None):  # type: Optional[Union[Marker, List[Marker]]]

        if is_above_python_2_7():
            super(MarkerFilter, self).__init__(name)

        else:
            Filter.__init__(self, name)

        warn("markersToMatch attribute will be replaced by a property. "
              "Use markers_to_match property instead.", DeprecationWarning)
        self.markers_to_match = markers

    def filter(self, record):
        # type: (LogRecord) -> bool
        """Filter by looking for a marker match.

        Args:
            record: A record to match with the filter(s).
        Returns:
            bool: Whether the record matched with the filter(s)
        """
        return match_markers(record, self.markers_to_match)
