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

from logging import Formatter, LogRecord
from deprecated import deprecated
from warnings import warn
from typing import Optional, Union, Dict

from onaplogging.utils.system import is_above_python_2_7, is_above_python_3_2
from onaplogging.utils.styles import (
    ATTRIBUTES,
    HIGHLIGHTS,
    COLORS,

    ATTRIBUTE_TAG,
    HIGHLIGHT_TAG,
    COLOR_TAG,

    RESET,
    FMT_STR
)


class BaseColorFormatter(Formatter):
    """Text color formatter class.

    Wraps the logging. Uses Git shell coloring codes.  Doesn't support Windows
    CMD yet. If `fmt` is not suppied, the `style` is used. Eventually converts
    a LogRecord object to "colored" text.

    TODO:
        Support for Windows CMD.
    Extends:
        logging.Formatter
    Properties:
        style       : '%', '{' or '$' formatting.
        datefrmt    : ISO8601-like (or RFC 3339-like) format.
    Args:
        fmt         : human-readable format.                  Defaults to None.
        datefmt     : ISO8601-like (or RFC 3339-like) format. Defaults to None.
        colorfmt    : Color schemas for logging levels.       Defaults to None.
        style       : '%', '{' or '$' formatting.             Defaults to '%'.
    Methods:
        format      : formats a LogRecord record.
        _parseColor : selects colors based on a logging levels.
    """

    @property
    def style(self):
        # type: () -> str
        return self.__style  # name mangling with __ to avoid accidents

    @property
    def colorfmt(self):
        # type: () -> str
        return self.__colorfmt

    @style.setter
    def style(self, value):
        # type: (str) -> None
        """Assign new style."""
        self.__style = value

    @colorfmt.setter
    def colorfmt(self, value):
        # type: (str) -> None
        """Assign new color format."""
        self.__colorfmt = value

    def __init__(self,
                 fmt=None,          # type: Optional[str]
                 datefmt=None,      # type: Optional[str]
                 colorfmt=None,     # type: Optional[Dict]
                 style="%"):        # type: Optional[str]

        if is_above_python_3_2():
            super(BaseColorFormatter, self). \
            __init__(fmt=fmt,  # noqa: E122
                    datefmt=datefmt,
                    style=style)

        elif is_above_python_2_7():
            super(BaseColorFormatter, self). \
            __init__(fmt, datefmt)  # noqa: E122

        else:
            Formatter. \
            __init__(self, fmt, datefmt)  # noqa: E122
        self.style = style
        self.colorfmt = colorfmt

    def format(self, record):
        """Text formatter.

        Connects 2 methods. First it extract a level and a colors
        assigned to  this level in  the BaseColorFormatter  class.
        Second it applied the colors to the text.

        Args:
            record   : an instance of a logged event.
        Returns:
            str      : "colored" text (formatted text).
        """

        if is_above_python_2_7():
            s = super(BaseColorFormatter, self). \
                format(record)

        else:
            s = Formatter. \
                format(self, record)

        color, highlight, attribute = self._parse_color(record)

        return apply_color(s, color, highlight, attrs=attribute)

    def _parse_color(self, record):
        # type: (LogRecord) -> (Optional[str], Optional[str], Optional[str])
        """Color formatter based on the logging level.

        This method formats the  record according to  its  level
        and a color format  set for that level.  If the level is
        not found, then this method will eventually return None.

        Args:
            record  : an instance of a logged event.
        Returns:
            str     : Colors.
            str     : Hightlight tag.
            str     : Attribute tag.
        """
        if  self.colorfmt and \
            isinstance(self.colorfmt, dict):

            level = record.levelname
            colors = self.colorfmt.get(level, None)

            if  colors is not None and \
                isinstance(colors, dict):
                return (colors.get(COLOR_TAG, None),        # noqa: E201
                        colors.get(HIGHLIGHT_TAG, None),
                        colors.get(ATTRIBUTE_TAG, None))    # noqa: E202
        return None, None, None

    @deprecated(reason="Will be removed. Use _parse_color(record) instead.")
    def _parseColor(self, record):
        """
        Color based on logging level.
        See method _parse_color(record).
        """
        return self._parse_color(record)


def apply_color(text,           # type: str
                color=None,     # type: Optional[str]
                on_color=None,  # type: Optional[str]
                attrs=None):    # type: Optional[Union[str, list]]
    # type: (...) -> str
    """Applies color codes to the text.

    Args:
        text      : text to be "colored" (formatted).
        color     : Color in human-readable format. Defaults to None.
        highlight : Hightlight color in human-readable format.
                         Previously called "on_color". Defaults to None.
        attrs     : Colors for attribute(s). Defaults to None.
    Returns:
        str       : "colored" text (formatted text).
    """
    warn("`on_color` will be replaced with `highlight`.", DeprecationWarning)
    highlight = on_color  # replace the parameter and remove

    if os.name in ('nt', 'ce'):
        return text

    if isinstance(attrs, str):
        attrs = [attrs]

    ansi_disabled = os.getenv('ANSI_COLORS_DISABLED', None)

    if ansi_disabled is None:

        if  color is not None and \
            isinstance(color, str):
            text = FMT_STR % (COLORS.get(color, 0), text)

        if  highlight is not None and \
            isinstance(highlight, str):
            text = FMT_STR % (HIGHLIGHTS.get(highlight, 0), text)

        if attrs is not None:
            for attr in attrs:
                text = FMT_STR % (ATTRIBUTES.get(attr, 0), text)

        text += RESET  # keep origin color for tail spaces

    return text


@deprecated(reason="Will be removed. Call apply_color(...) instead.")
def colored(text, color=None, on_color=None, attrs=None):
    """
    Format text with color codes.
    See method apply_color(text, color, on_color, attrs).
    """
    return apply_color(text, color, on_color, attrs)
