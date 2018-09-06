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
from marker import MARKER_TAG
from marker import Marker


class MarkerFormatter(logging.Formatter):

    def __init__(self, fmt=None, datefmt=None, style='%'):

        if sys.version_info > (3,2):
            super(MarkerFormatter, self).__init__(
                fmt=fmt, datefmt=datefmt, style=style)
        elif sys.version_info > (2, 7):
            super(MarkerFormatter, self).__init__(
                fmt=fmt, datefmt=datefmt)
        else:
            logging.Formatter.__init__(self, fmt, datefmt)

        self.style = style
        self._marker_tag = "%(marker)s"

        if sys.version_info > (3, 2):
            if self.style not in logging._STYLES:
                raise ValueError('Style must be one of: %s' %
                                 ','.join(logging._STYLES.keys()))
            if self.style == "{":
                self._marker_tag = "{marker}"
            elif self.style == "$":
                self._marker_tag = "${marker}"

        self._tmpFmt = self._fmt

    def format(self, record):

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
                return super(MarkerFormatter,self).format(record)
            else:
                return logging.Formatter.format(self, record)

        finally:
                self._fmt = self._tmpFmt