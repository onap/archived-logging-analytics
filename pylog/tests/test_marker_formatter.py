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
from collections import namedtuple

if sys.version_info[0] < 3:
    from mock import patch
if sys.version_info[0] >= 3:
    from unittest.mock import patch

import pytest

from onaplogging.marker import BaseMarker
from onaplogging.markerFormatter import MarkerFormatter


class TestMarkerFormatter(unittest.TestCase):

    Record = namedtuple("Record", "marker")

    def test_marker_formatter_init(self):
        marker_formatter = MarkerFormatter()
        self.assertEqual(marker_formatter.style, "%")
        self.assertEqual(marker_formatter.marker_tag, "%(marker)s")

        if sys.version_info[0] >= 3:
            marker_formatter = MarkerFormatter(style="{")
            self.assertEqual(marker_formatter.style, "{")
            self.assertEqual(marker_formatter.marker_tag, "{marker}")

            marker_formatter = MarkerFormatter(style="$")
            self.assertEqual(marker_formatter.style, "$")
            self.assertEqual(marker_formatter.marker_tag, "${marker}")

            with pytest.raises(ValueError):
                MarkerFormatter(style="*")

    def test_marker_formatter_format(self):
        record = self.Record(BaseMarker("test"))

        with patch("onaplogging.markerFormatter.BaseColorFormatter.format") as mock_format:
            marker_formatter = MarkerFormatter()
            self.assertEqual(marker_formatter._fmt, "%(message)s")
            self.assertEqual(marker_formatter.marker_tag, "%(marker)s")
            marker_formatter.format(record)
            mock_format.assert_called_once()
            self.assertEqual(marker_formatter._fmt, "%(message)s")
            self.assertEqual(marker_formatter.marker_tag, "%(marker)s")

        with patch("onaplogging.markerFormatter.BaseColorFormatter.format") as mock_format:
            marker_formatter = MarkerFormatter(fmt="%(message)s %(marker)s")
            self.assertEqual(marker_formatter._fmt, "%(message)s %(marker)s")
            self.assertEqual(marker_formatter.marker_tag, "%(marker)s")
            marker_formatter.format(record)
            mock_format.assert_called_once()
            self.assertEqual(marker_formatter._fmt, "%(message)s %(marker)s")
            self.assertEqual(marker_formatter.marker_tag, "%(marker)s")
            self.assertEqual(marker_formatter.temp_fmt, "%(message)s %(marker)s")
