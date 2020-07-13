# Copyright (c) 2020 Deutsche Telekom.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

import sys
import unittest

if sys.version_info[0] < 3:
    from mock import MagicMock, patch
if sys.version_info[0] >= 3:
    from unittest.mock import MagicMock, patch

import pytest

from onaplogging.mdcformatter import MDCFormatter


class TestMdcFormatter(unittest.TestCase):

    def test_mdc_formatter_init(self):
        mdc_formatter = MDCFormatter()
        self.assertEqual(mdc_formatter.style, "%")
        self.assertEqual(mdc_formatter._mdc_tag, "%(mdc)s")
        self.assertEqual(mdc_formatter._mdcFmt, "{reqeustID}")

        mdc_formatter = MDCFormatter(mdcfmt="{test}")
        self.assertEqual(mdc_formatter.style, "%")
        self.assertEqual(mdc_formatter._mdc_tag, "%(mdc)s")
        self.assertEqual(mdc_formatter._mdcFmt, "{test}")

        if sys.version_info[0] >= 3:
            mdc_formatter = MDCFormatter(style="{")
            self.assertEqual(mdc_formatter.style, "{")
            self.assertEqual(mdc_formatter._mdc_tag, "{mdc}")
            self.assertEqual(mdc_formatter._mdcFmt, "{reqeustID}")

            mdc_formatter = MDCFormatter(style="$")
            self.assertEqual(mdc_formatter.style, "$")
            self.assertEqual(mdc_formatter._mdc_tag, "${mdc}")
            self.assertEqual(mdc_formatter._mdcFmt, "{reqeustID}")

            with pytest.raises(ValueError):
                MDCFormatter(style="*")

    def test_mdc_fmt_key(self):
        mdc_formatter = MDCFormatter()
        brace, not_brace = mdc_formatter._mdcfmtKey()
        self.assertEqual(brace, ["{reqeustID}"])
        self.assertEqual(list(not_brace), ["reqeustID"])

        mdc_formatter = MDCFormatter(mdcfmt="{test} {value} {anything}")
        brace, not_brace = mdc_formatter._mdcfmtKey()
        self.assertEqual(brace, ["{test}", "{value}", "{anything}"])
        self.assertEqual(list(not_brace), ["test", "value", "anything"])

        mdc_formatter = MDCFormatter(mdcfmt="no_braces")
        brace, not_brace = mdc_formatter._mdcfmtKey()
        self.assertEqual(brace, [])
        self.assertIsNone(not_brace)

        mdc_formatter = MDCFormatter(mdcfmt="}what?{")
        brace, not_brace = mdc_formatter._mdcfmtKey()
        self.assertEqual(brace, [])
        self.assertIsNone(not_brace)

        mdc_formatter = MDCFormatter(mdcfmt="}{hello}{")
        brace, not_brace = mdc_formatter._mdcfmtKey()
        self.assertEqual(brace, ["{hello}"])
        self.assertEqual(list(not_brace), ["hello"])

        mdc_formatter = MDCFormatter(mdcfmt="}{}{hel{lo}{")
        brace, not_brace = mdc_formatter._mdcfmtKey()
        self.assertEqual(brace, [])
        self.assertIsNone(not_brace)
    
    def test_format(self):
        record = MagicMock()
        with patch("onaplogging.mdcformatter.MarkerFormatter.format") as mock_marker_formatter_format:
            mdc_formatter = MDCFormatter()
            mdc_formatter.format(record)
            mock_marker_formatter_format.assert_called_once_with(record)
            self.assertEqual(mdc_formatter._fmt, "%(message)s")

        if sys.version_info[0] >= 3:
            with patch("onaplogging.mdcformatter.MarkerFormatter.format") as mock_marker_formatter_format:
                mdc_formatter = MDCFormatter(fmt="{mdc}", style="{", mdcfmt="{key}")
                mdc_formatter.format(record)
                mock_marker_formatter_format.assert_called_once_with(record)
                self.assertEqual(mdc_formatter._fmt, "key=")

            record.mdc = {"key": 123}
            with patch("onaplogging.mdcformatter.MarkerFormatter.format") as mock_marker_formatter_format:
                mdc_formatter = MDCFormatter(fmt="{mdc}", style="{", mdcfmt="no_braces")
                mdc_formatter.format(record)
                mock_marker_formatter_format.assert_called_once_with(record)
                self.assertEqual(mdc_formatter._fmt, "")

            with patch("onaplogging.mdcformatter.MarkerFormatter.format") as mock_marker_formatter_format:
                mdc_formatter = MDCFormatter(fmt="{mdc}", style="{", mdcfmt="{key}")
                mdc_formatter.format(record)
                mock_marker_formatter_format.assert_called_once_with(record)
                self.assertEqual(mdc_formatter._fmt, "key=123")
