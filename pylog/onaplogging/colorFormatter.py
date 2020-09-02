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
from logging import Formatter

from .utils import is_above_python_2_7, is_above_python_3_2


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

RESET = "\033[0m"
FMT_STR = "\033[%dm%s"


class BaseColorFormatter(Formatter):
    """Text color formatter class.

    Wraps the logging. Uses Git shell coloring codes. Doesn't support Windows
    CMD yet. If `fmt` is not suppied, the `style` is used. Eventually converts
    a LogEvent object to "colored" text.

    TODO: Support for Windows CMD.

    Extends:
        logging.Formatter

    Args:
        fmt (str, optional): human-readable format. Defaults to None.
        datefmt (str, optional): ISO8601-like (or RFC 3339-like) format.
                                        Defaults to None.
        colorfmt (dict, optional): Color schemas for logging levels.
                                        Defaults to None.
        style (str, optional): '%', '{' or '$' formatting. Defaults to '%'.

    Methods:
        format: formats a LogEvent record.
        _parseColor: selects colors based on a logging levels.
    """

    def __init__(self, fmt=None, datefmt=None, colorfmt=None, style="%"):
        if is_above_python_3_2():
            super(BaseColorFormatter, self).__init__(
                fmt=fmt, datefmt=datefmt, style=style)
        elif is_above_python_2_7():
            super(BaseColorFormatter, self).__init__(fmt, datefmt)
        else:
            Formatter.__init__(self, fmt, datefmt)

        self.style = style
        self.colorfmt = colorfmt

    def format(self, record):
        """Text formatter.

        Connects 2 methods. First it extract a level and a colors
        assigned to this level in the BaseColorFormatter class.
        Second it applied the colors to the text.

        Args:
            record (LogEvent): an instance of a logged event.

        Returns:
            str: "colored" text (formatted text).
        """
        if sys.version_info > (2, 7):
            s = super(BaseColorFormatter, self).format(record)
        else:
            s = Formatter.format(self, record)
        color, on_color, attribute = self._parseColor(record)
        return colored(s, color, on_color, attrs=attribute)

    def _parseColor(self, record):
        """Color formatter based on the logging level.

        This method formats the record according to its level
        and a color format set for that level. If the level is
        not found, then this method will eventually return None.

        Args:
            record (LogRecord): an instance of a logged event.

        Returns:
            str: Colors.
            str: Hightlight tag.
            str: Attribute tag.
        """
        if self.colorfmt and isinstance(self.colorfmt, dict):

            level = record.levelname
            colors = self.colorfmt.get(level, None)

            if colors is not None and isinstance(colors, dict):
                return colors.get(COLOR_TAG, None), \
                       colors.get(HIGHLIGHT_TAG, None), \
                       colors.get(ATTRIBUTE_TAG, None)

        return None, None, None


def colored(text, color=None, on_color=None, attrs=None):
    """Applies colors codes to the text.

    Args:
        text (str): tex to be "colored" (formatted).
        color (str, optional): Color in human-readable format.
                                    Defaults to None.
        on_color (str, optional): Hightlight color in human-readable format.
                                    Defaults to None.
        attrs (str/list, optional): Colors for an attribute (list of
                                    attributes). Defaults to None.

    Returns:
        str: "colored" text (formatted text).
    """
    if os.name in ('nt', 'ce'):
        return text

    if isinstance(attrs, str):
        attrs = [attrs]

    if os.getenv('ANSI_COLORS_DISABLED', None) is None:
        if color is not None and isinstance(color, str):
            text = FMT_STR % (COLORS.get(color, 0), text)

        if on_color is not None and isinstance(on_color, str):
            text = FMT_STR % (HIGHLIGHTS.get(on_color, 0), text)

        if attrs is not None:
            for attr in attrs:
                text = FMT_STR % (ATTRIBUTES.get(attr, 0), text)

        text += RESET  # keep origin color for tail spaces

    return text
