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
from .markerFormatter import MarkerFormatter
from .utils import is_above_python_2_7, is_above_python_3_2


class MDCFormatter(MarkerFormatter):
    """A custom MDC formatter.

    Prepares Mapped Diagnostic Context to enrich log message. If `fmt` is not
    supplied, the `style` is used.

    Extends:
        MarkerFormatter.

    Attributes:
        fmt (str): Built-in format string containing standard Python %-style
                            mapping keys in human-readable format.
        mdcFmt (str): MDC format with '{}'-style mapping keys.
        datefmt (str): Date format.
        colorfmt (str): colored output with an ANSI terminal escape code.
        style (str): style mapping keys in Python 3.x.
    """

    def __init__(self, fmt=None, mdcfmt=None,
                 datefmt=None, colorfmt=None, style="%"):
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
            MarkerFormatter.__init__(self, fmt, datefmt, colorfmt)

        self._mdc_tag = "%(mdc)s"

        if self.style == "{":
            self._mdc_tag = "{mdc}"
        elif self.style == "$":
            self._mdc_tag = "${mdc}"

        if mdcfmt:
            self._mdcFmt = mdcfmt
        else:
            self._mdcFmt = '{reqeustID}'

    def _mdcfmtKey(self):
        """Maximum (balanced) parantehses matching algorithm for MDC keys.

        Extracts and strips keys and words from a MDC format string. Use this
        method to find the MDC key.

        Returns:
            list: list of keys.
            map object: keys with and without brace, such as ({key}, key).
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

    def _replaceStr(self, keys):
        """
            Removes the first and last characters from the kemdcFmtkeys and
            assigns the not stripped key.
        """
        fmt = self._mdcFmt
        for key in keys:
            fmt = fmt.replace(key, key[1:-1] + "=" + key)
        return fmt

    def format(self, record):
        """
        Find MDCs in a log record's extra field. If the key from mdcFmt
        doesn't contain MDC, the values will be empty.

        For example:
        The MDC dict in a logging record is {'key1':'value1','key2':'value2'}.
        The mdcFmt is '{key1} {key3}'.
        The output of MDC message is 'key1=value1 key3='.

        Args:
            record (LogEvent): an instance of a logged event.

        Returns:
            str: "colored" text (formatted text).
        """
        mdcIndex = self._fmt.find(self._mdc_tag)
        if mdcIndex == -1:
            if is_above_python_2_7():
                return super(MDCFormatter, self).format(record)
            else:
                return MarkerFormatter.format(self, record)

        mdcFmtkeys, mdcFmtWords = self._mdcfmtKey()

        if mdcFmtWords is None:
            self._fmt = self._fmt.replace(self._mdc_tag, "")
            if is_above_python_3_2():
                self._style = logging._STYLES[self.style][0](self._fmt)

            if is_above_python_2_7():
                return super(MDCFormatter, self).format(record)
            else:
                return MarkerFormatter.format(self, record)

        mdc = record.__dict__.get('mdc', None)
        res = {}
        for i in mdcFmtWords:
            if mdc and i in mdc:
                res[i] = mdc[i]
            else:
                res[i] = ""

        del mdc
        try:
            mdcstr = self._replaceStr(keys=mdcFmtkeys).format(**res)
            self._fmt = self._fmt.replace(self._mdc_tag, mdcstr)

            if is_above_python_3_2():
                self._style = logging._STYLES[self.style][0](self._fmt)

            if is_above_python_2_7():
                return super(MDCFormatter, self).format(record)
            else:
                return MarkerFormatter.format(self, record)

        except KeyError as e:
            # Are prints instead of raises necessary?
            print("The mdc key %s format is wrong" % str(e))
        except Exception:
            raise
