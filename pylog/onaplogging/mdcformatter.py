# Copyright (c) 2018 VMware, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

import logging


class MDCFormatter(logging.Formatter):
    """
    A custom MDC formatter to prepare Mapped Diagnostic Context
    to enrich log message.
    """

    def __init__(self, fmt=None, mdcfmt=None, datefmt=None):
        """
        :param fmt: build-in format string contains standard
               Python %-style mapping keys
        :param mdcFmt: mdc format with '{}'-style mapping keys
        :param datefmt: Date format to use
        """

        super(MDCFormatter, self).__init__(fmt=fmt, datefmt=datefmt)
        self._tmpfmt = self._fmt
        if mdcfmt:
            self._mdcFmt = mdcfmt
        else:
            self._mdcFmt = '{reqeustID}'

    def _mdcfmtKey(self):
        """
         maximum barce match algorithm to find the mdc key
        :return: key in brace  and key not in brace,such as ({key}, key)
        """

        left = '{'
        right = '}'
        target = self._mdcFmt
        st = []
        keys = []
        for index, v in enumerate(target):
            if v == left:
                st.append(index)
            elif v == right:

                if len(st) == 0:
                    continue

                elif len(st) == 1:
                    start = st.pop()
                    end = index
                    keys.append(target[start:end + 1])
                elif len(st) > 0:
                    st.pop()

        keys = filter(lambda x: x[1:-1].strip('\n \t  ') != "", keys)
        words = None
        if keys:
            words = map(lambda x: x[1:-1], keys)

        return keys, words

    def _replaceStr(self, keys):

        fmt = self._mdcFmt
        for i in keys:
            fmt = fmt.replace(i, i[1:-1] + "=" + i)

        return fmt

    def format(self, record):
        """
        Find mdcs in log record extra field, if key form mdcFmt dosen't
        contains mdcs, the values will be empty.
        :param record: the logging record instance
        :return:  string
        for example:
            the mdcs dict in logging record is
            {'key1':'value1','key2':'value2'}
            the mdcFmt is" '{key1} {key3}'
            the output of mdc message: 'key1=value1 key3='

        """
        mdcIndex = self._fmt.find('%(mdc)s')
        if mdcIndex == -1:
            return super(MDCFormatter, self).format(record)

        mdcFmtkeys, mdcFmtWords = self._mdcfmtKey()
        if mdcFmtWords is None:

            self._fmt = self._fmt.replace("%(mdc)s", "")
            return super(MDCFormatter, self).format(record)

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
            self._fmt = self._fmt.replace("%(mdc)s", mdcstr)
            s = super(MDCFormatter, self).format(record)
            return s

        except KeyError as e:
            print("The mdc key %s format is wrong" % e.message)
        except Exception:
            raise

        finally:
            # reset fmt format
            self._fmt = self._tmpfmt
