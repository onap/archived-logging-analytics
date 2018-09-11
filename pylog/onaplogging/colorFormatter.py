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

import os
import sys
import logging
from logging import Formatter


ATTRIBUTES = {
    'normal': 0,
    'bold': 1,
    'underline': 4,
    'blink': 5,
    'invert': 7,
    'hide': 8,

}


HIGHLIGHTS = {

    'black': 40,
    'red':  41,
    'green': 42,
    'yellow': 43,
    'blue': 44,
    'purple': 45,
    'cyan': 46,
    'white': 47,
}

COLORS = {

    'black': 30,
    'red': 31,
    'green': 32,
    'yellow': 33,
    'blue': 34,
    'purple': 35,
    'cyan': 36,
    'white': 37,
}

COLOR_TAG = "color"
HIGHLIGHT_TAG = "highlight"
ATTRIBUTE_TAG = "attribute"

RESET = '\033[0m'


def colored(text, color=None, on_color=None, attrs=None):
    # It can't support windows system cmd right now!
    # TODO: colered output on windows system cmd
    if os.name in ('nt', 'ce'):
        return text

    if isinstance(attrs, str):
        attrs = [attrs]

    if os.getenv('ANSI_COLORS_DISABLED', None) is None:
        fmt_str = '\033[%dm%s'
        if color is not None and isinstance(color, str):
            text = fmt_str % (COLORS.get(color, 0), text)

        if on_color is not None and isinstance(on_color, str):
            text = fmt_str % (HIGHLIGHTS.get(on_color, 0), text)

        if attrs is not None:
            for attr in attrs:
                text = fmt_str % (ATTRIBUTES.get(attr, 0), text)

        #  keep origin color for tail spaces
        text += RESET
    return text


class BaseColorFormatter(Formatter):

    def __init__(self, fmt=None, datefmt=None, colorfmt=None, style="%"):
        if sys.version_info > (3, 2):
            super(BaseColorFormatter, self).__init__(
                fmt=fmt, datefmt=datefmt, style=style)
        elif sys.version_info > (2, 7):
            super(BaseColorFormatter, self).__init__(fmt, datefmt)
        else:
            Formatter.__init__(self, fmt, datefmt)

        self.style = style
        if sys.version_info > (3, 2):
            if self.style not in logging._STYLES:
                raise ValueError('Style must be one of: %s' % ','.join(
                        logging._STYLES.keys()))

        self.colorfmt = colorfmt

        print(self.colorfmt, isinstance(self.colorfmt, dict))

    def _parseColor(self, record):
        """
        color formatter for instance:
        {
            "logging-levelname":
                {
                    "color":"<COLORS>",
                    "highlight":"<HIGHLIGHTS>",
                    "attribute":"<ATTRIBUTES>",
                }
        }
        :param record:
        :return: text color, background color, text attribute
        """
        if self.colorfmt and isinstance(self.colorfmt, dict):

            level = record.levelname
            colors = self.colorfmt.get(level, None)

            if colors is not None and isinstance(colors, dict):
                return colors.get(COLOR_TAG, None), \
                       colors.get(HIGHLIGHT_TAG, None), \
                       colors.get(ATTRIBUTE_TAG, None)

        return None, None, None

    def format(self, record):

        if sys.version_info > (2, 7):
            s = super(BaseColorFormatter, self).format(record)
        else:
            s = Formatter.format(self, record)
        color, on_color, attribute = self._parseColor(record)
        return colored(s, color, on_color, attrs=attribute)
