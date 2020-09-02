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
from logging import Filter
from .marker import matchMarkerHelp


class MarkerFilter(Filter):
    """Marker filtering.

    Extends:
        logging.Filter

    Attributes:
        markerToMatch (Marker/list): a marker of list of markers.

    Methods
        filter: Filter records by the current filter marker(s).
    """

    def __init__(self, name="", markers=None):
        if sys.version_info > (2, 7):
            super(MarkerFilter, self).__init__(name)
        else:
            Filter.__init__(self, name)

        self.markerToMatch = markers

    def filter(self, record):
        """Filter by looking for a match.

        Args:
            record (LogEvent): A record to match with the filter(s).

        Returns:
            bool: Whether the record matched with the filter(s)
        """
        return matchMarkerHelp(record, self.markerToMatch)
