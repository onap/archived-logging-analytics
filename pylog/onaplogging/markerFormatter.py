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

import logging
from logging import LogRecord
from typing import Optional

from onaplogging.utils.styles import MARKER_OPTIONS
from onaplogging.utils.system import is_above_python_2_7, is_above_python_3_2

from .marker import Marker, MARKER_TAG
from .colorFormatter import BaseColorFormatter


class MarkerFormatter(BaseColorFormatter):
    """Formats coloring styles based on a marker.

    If `fmt` is not supplied, the `style` is used.

    Extends:
        BaseColorFormatter
    Properties:
        marker_tag: a marker to be applied.
        temp_fmt  : keeps initial format to be reset to after formatting.
    Args:
        fmt       : human-readable format.                    Defaults to None.
        datefmt   : ISO8601-like (or RFC 3339-like) format.   Defaults to None.
        colorfmt  : color schemas for logging levels.         Defaults to None.
        style     : '%', '{' or '$' formatting.               Defaults to '%'.
                      Added in Python 3.2.
    """

    @property
    def marker_tag(self):
        # type: () -> str
        return self._marker_tag

    @property
    def temp_fmt(self):
        # type: () -> str
        return self._temp_fmt

    @marker_tag.setter
    def marker_tag(self, value):
        # type: (str) -> None
        self._marker_tag = value

    @temp_fmt.setter
    def temp_fmt(self, value):
        # type: (str) -> None
        self._temp_fmt = value

    def __init__(self,
                 fmt=None,          # type: Optional[str]
                 datefmt=None,      # type: Optional[str]
                 colorfmt=None,     # type: Optional[dict]
                 style='%'):        # type: Optional[str]

        if is_above_python_3_2():
            super(MarkerFormatter, self).\
            __init__(fmt=fmt,  # noqa: E122
                     datefmt=datefmt,
                     colorfmt=colorfmt,
                     style=style)  # added in Python 3.2+

        elif is_above_python_2_7():
            super(MarkerFormatter, self).\
            __init__(fmt=fmt,  # noqa: E122
                     datefmt=datefmt,
                     colorfmt=colorfmt)

        else:
            BaseColorFormatter.\
            __init__(self, fmt, datefmt, colorfmt)  # noqa: E122

        self.marker_tag = MARKER_OPTIONS[self.style]
        self.temp_fmt = self._fmt

    def format(self, record):
        # type: (LogRecord) -> str
        """Marker formatter.

        Use it to apply the marker from the LogRecord record to the formatter
        string `fmt`.

        Args:
            record  : an instance of a logged event.
        Returns:
            str     : "colored" text (formatted text).
        """
        try:

            if  self._fmt.find(self.marker_tag) != -1 and \
                hasattr(record, MARKER_TAG):
                marker = getattr(record, MARKER_TAG)

                if isinstance(marker, Marker):
                    self._fmt = self._fmt.replace(self.marker_tag,
                                                  marker.name)

            elif self._fmt.find(self.marker_tag) != -1 and \
                 not hasattr(record, MARKER_TAG):
                self._fmt = self._fmt.replace(self.marker_tag, "")

            if is_above_python_3_2():
                StylingClass = logging._STYLES[self.style][0]
                self.style = StylingClass(self._fmt)

            if is_above_python_2_7():
                # includes Python 3.2+ style attribute
                return super(MarkerFormatter, self).format(record)
            else:
                return BaseColorFormatter.format(self, record)

        finally:
            self._fmt = self.temp_fmt
