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
import logging
from .marker import MARKER_TAG
from .marker import Marker
from .colorFormatter import BaseColorFormatter


class MarkerFormatter(BaseColorFormatter):
    """Formats coloring styles based on a marker.

    If `fmt` is not supplied, the `style` is used.

    Extends:
        BaseColorFormatter

    Args:
        fmt (str, optional): human-readable format. Defaults to None.
        datefmt (str, optional): ISO8601-like (or RFC 3339-like) format.
                                    Defaults to None.
        colorfmt (dict, optional): Color schemas for logging levels.
                                    Defaults to None.
        style (str, optional): '%', '{' or '$' formatting. Defaults to '%'.
    """

    def __init__(self, fmt=None, datefmt=None, colorfmt=None, style='%'):

        if sys.version_info > (3, 2):
            super(MarkerFormatter, self).__init__(
                fmt=fmt, datefmt=datefmt, colorfmt=colorfmt, style=style)
        elif sys.version_info > (2, 7):
            super(MarkerFormatter, self).__init__(
                fmt=fmt, datefmt=datefmt, colorfmt=colorfmt)
        else:
            BaseColorFormatter.__init__(self, fmt, datefmt, colorfmt)

        if self.style == "%":
            self._marker_tag = "%(marker)s"
        elif self.style == "{":
            self._marker_tag = "{marker}"
        elif self.style == "$":
            self._marker_tag = "${marker}"

        self._tmpFmt = self._fmt

    def format(self, record):
        """Marker formatter.

        Use it to apply the marker from a LogEvent record
        to the formatter string - `fmt`.

        Args:
            record (LogEvent): an instance of a logged event.

        Returns:
            str: "colored" text (formatted text).
        """
        try:
            if self._fmt.find(self._marker_tag) != -1 \
                    and hasattr(record, MARKER_TAG):
                marker = getattr(record, MARKER_TAG)

                if isinstance(marker, Marker):
                    self._fmt = self._fmt.replace(
                        self._marker_tag, marker.getName())
            elif self._fmt.find(self._marker_tag) != -1 \
                    and not hasattr(record, MARKER_TAG):

                self._fmt = self._fmt.replace(self._marker_tag, "")

            if sys.version_info > (3, 2):
                self._style = logging._STYLES[self.style][0](self._fmt)

            if sys.version_info > (2, 7):
                return super(MarkerFormatter, self).format(record)
            else:
                return BaseColorFormatter.format(self, record)

        finally:
            self._fmt = self._tmpFmt
