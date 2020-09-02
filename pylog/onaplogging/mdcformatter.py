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
from typing import Mapping, List, Dict, Callable
from deprecated import deprecated

from onaplogging.utils.system import is_above_python_2_7, is_above_python_3_2
from onaplogging.utils.styles import MDC_OPTIONS

from .markerFormatter import MarkerFormatter


class MDCFormatter(MarkerFormatter):
    """A custom MDC formatter.

    Prepares Mapped Diagnostic Context to enrich log message. If `fmt` is not
    supplied, the `style` is used.

    Extends:
        MarkerFormatter
    Args:
        fmt             : Built-in format string containing standard Python
                            %-style mapping keys in human-readable format.
        mdcFmt          : MDC format with '{}'-style mapping keys.
        datefmt         : Date format.
        colorfmt        : colored output with an ANSI terminal escape code.
        style           : style mapping keys in Python 3.x.
    """

    @property
    def mdc_tag(self):
        # type: () -> str
        return self._mdc_tag

    @property
    def mdcfmt(self):
        # type: () -> str
        return self._mdcFmt

    @mdc_tag.setter
    def mdc_tag(self, value):
        # type: (str) -> str
        self._mdc_tag = value

    @mdcfmt.setter
    def mdcfmt(self, value):
        # type: (str) -> str
        self._mdc_tag = value

    def __init__(self,
                 fmt=None,          # type: str
                 mdcfmt=None,       # type: str
                 datefmt=None,      # type: str
                 colorfmt=None,     # type: str
                 style="%"):        # type: str

        if is_above_python_3_2():
            super(MDCFormatter, self).__init__(fmt=fmt,
                                               datefmt=datefmt,
                                               colorfmt=colorfmt,
                                               style=style)
        elif is_above_python_2_7():
            super(MDCFormatter, self).__init__(fmt=fmt,
                                               datefmt=datefmt,
                                               colorfmt=colorfmt)
        else:
            MarkerFormatter.\
            __init__(self, fmt, datefmt, colorfmt)  # noqa: E122

        self._mdc_tag = MDC_OPTIONS[self.style]
        self._mdcFmt = mdcfmt if mdcfmt else '{reqeustID}'

    def format(self, record):
        # type: (LogRecord) -> str
        """
        Find MDCs in a log record's extra field. If the key from mdcFmt
        doesn't contain MDC, the values will be empty.

        For example:
        The MDC dict in a logging record is {'key1':'value1','key2':'value2'}.
        The mdcFmt is '{key1} {key3}'.
        The output of MDC message is 'key1=value1 key3='.

        Args:
            record  : an instance of a logged event.
        Returns:
            str     : "colored" text (formatted text).
        """

        mdc_index = self._fmt.find(self._mdc_tag)
        if mdc_index == -1:
            return self._parent_format(record)

        mdc_format_keys, mdc_format_words = self._mdc_format_key()

        if mdc_format_words is None:
            self._fmt = self._replace_mdc_tag_str("")
            self._apply_styling()

            return self._parent_format(record)

        res = self._apply_mdc(record, mdc_format_words)

        try:
            mdc_string = self._replaceStr(keys=mdc_format_keys).format(**res)
            self._fmt = self._replace_mdc_tag_str(mdc_string)
            self._apply_styling()

            return self._parent_format(record)

        except KeyError as e:
            # is there a need for print?
            print("The mdc key %s format is wrong" % str(e))

        except Exception:
            raise

    def _mdc_format_key(self):
        # type: () -> (List, Mapping[str, str])
        """Maximum (balanced) parantehses matching algorithm for MDC keys.

        Extracts and strips keys and words from a MDC format string. Use this
        method to find the MDC key.

        Returns:
            list        : list of keys.
            map object  : keys with and without brace, such as ({key}, key).
        """

        left = '{'
        right = '}'
        target = self._mdcFmt
        stack = []
        keys = []

        for index, v in enumerate(target):
            if v == left:
                stack.append(index)
            elif v == right:

                if len(stack) == 0:
                    continue

                elif len(stack) == 1:
                    start = stack.pop()
                    end = index
                    keys.append(target[start:end + 1])
                elif len(stack) > 0:
                    stack.pop()

        keys = list(filter(lambda x: x[1:-1].strip('\n \t  ') != "", keys))
        words = None

        if keys:
            words = map(lambda x: x[1:-1], keys)

        return keys, words

    def _replace_string(self, keys):
        # type: (List[str]) -> str
        """
        Removes the first and last characters from each key and assigns not
        stripped keys.
        """
        fmt = self._mdcFmt
        for key in keys:
            fmt = fmt.replace(key, key[1:-1] + "=" + key)
        return fmt

    def _parent_format(self, record):
        # type: (LogRecord) -> str
        """Call super class's format based on Python version."""
        if is_above_python_2_7():
            return super(MDCFormatter, self).format(record)
        else:
            return MarkerFormatter.format(self, record)

    def _apply_mdc(self, record, mdc_format_words):
        # type: (LogRecord, Mapping[Callable[[str], str], List]) -> Dict
        """Apply MDC pamming to the LogRecord record."""
        mdc = record.__dict__.get('mdc', None)
        res = {}

        for i in mdc_format_words:
            if mdc and i in mdc:
                res[i] = mdc[i]
            else:
                res[i] = ""
        del mdc
        return res

    def _apply_styling(self):
        # type: () -> None
        """Apply styling to the formatter if using Python 3.2+"""
        if is_above_python_3_2():
            StylingClass = logging._STYLES[self.style][0](self._fmt)
            self._style = StylingClass

    def _replace_mdc_tag_str(self, replacement):
        # type: (str) -> str
        """Replace MDC tag in the format string."""
        return self._fmt.replace(self._mdc_tag, replacement)

    @deprecated(reason="Will be replaced. Use _mdc_format_key() instead.")
    def _mdcfmtKey(self):
        """See _mdc_format_key()."""
        return self._mdc_format_key()

    @deprecated(reason="Will be replaced. Use _replace_string(keys) instead.")
    def _replaceStr(self, keys):
        """See _replace_string(keys)."""
        return self._replace_string(keys)
