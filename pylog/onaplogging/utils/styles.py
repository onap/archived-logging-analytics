# Copyright (c) 2020 Deutsche Telekom.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

"""These are ANSI shell coloring codes used to format strings.

[ begins the color definition. \033 starts the escape sequence.
[\0330m is the default color of the shell that closes the escape sequence.

`FMT_STR` takes the color as its first parameter (int). As the second
parameter its takes the text (str).

TL;DR
    Examples on ANSI colors, attributes, backgrounds and foregrounds:
    https://stackoverflow.com/a/28938235/7619961
"""

COLOR_TAG = "color"
HIGHLIGHT_TAG = "highlight"
ATTRIBUTE_TAG = "attribute"

RESET = "\033[0m"
FMT_STR = "\033[%dm%s"

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
    'red': 41,
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

"""
MDC and MARKER options are used only with Python starting 3.2 due to an update
in the logging module. This allows the use of %-formatting, :meth:`str.format`
(``{}``) formatting or :class:`string.Template` in the format string.
"""

MARKER_OPTIONS = {
    "%": "%(marker)s",
    "{": "{marker}",
    "$": "${marker}"
}

MDC_OPTIONS = {
    "%": "%(mdc)s",
    "{": "{mdc}",
    "$": "${mdc}"
}
