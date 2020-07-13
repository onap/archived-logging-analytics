# Copyright (c) 2020 Deutsche Telekom.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

import unittest
import sys
from logging import LogRecord

if sys.version_info[0] < 3:
    from mock import patch
if sys.version_info[0] >= 3:
    from unittest.mock import patch
import pytest

from onaplogging.colorFormatter import (
    ATTRIBUTES,
    BaseColorFormatter,
    colored,
    COLORS,
    HIGHLIGHTS,
    FMT_STR,
    RESET,
)
from onaplogging.utils import is_above_python_3_2


class TestColorFormatter(unittest.TestCase):

    TEST_TEXT = "test"

    def test_colored_os_name_nt(self):

        with patch("onaplogging.colorFormatter.os.name", "nt"):

            text = colored(self.TEST_TEXT)
            assert text == self.TEST_TEXT

            text = colored(self.TEST_TEXT, color="black")
            assert text == self.TEST_TEXT

            text = colored(self.TEST_TEXT, on_color="black")
            assert text == self.TEST_TEXT

            text = colored(self.TEST_TEXT, attrs="bold")
            assert text == self.TEST_TEXT

    def test_colored_os_name_ce(self):

        with patch("onaplogging.colorFormatter.os.name", "ce"):

            text = colored(self.TEST_TEXT)
            assert text == self.TEST_TEXT

            text = colored(self.TEST_TEXT, color="black")
            assert text == self.TEST_TEXT

            text = colored(self.TEST_TEXT, on_color="black")
            assert text == self.TEST_TEXT

            text = colored(self.TEST_TEXT, attrs="bold")
            assert text == self.TEST_TEXT

    def test_colored_os_name_posix(self):

        with patch("onaplogging.colorFormatter.os.name", "posix"):
            text = colored(self.TEST_TEXT)
            assert text == self.TEST_TEXT + RESET

            text = colored(self.TEST_TEXT, color="black")
            assert text == FMT_STR % (COLORS["black"], self.TEST_TEXT) + RESET

            text = colored(self.TEST_TEXT, color="invalid")
            assert text == FMT_STR % (0, self.TEST_TEXT) + RESET

            text = colored(self.TEST_TEXT, on_color="red")
            assert text == FMT_STR % (HIGHLIGHTS["red"], self.TEST_TEXT) + RESET

            text = colored(self.TEST_TEXT, on_color="invalid")
            assert text == FMT_STR % (0, self.TEST_TEXT) + RESET

            text = colored(self.TEST_TEXT, attrs="bold")
            assert text == FMT_STR % (ATTRIBUTES["bold"], self.TEST_TEXT) + RESET

            text = colored(self.TEST_TEXT, attrs=["bold", "blink"])
            assert (
                text
                == FMT_STR % (ATTRIBUTES["blink"], FMT_STR % (ATTRIBUTES["bold"], self.TEST_TEXT))
                + RESET
            )

            text = colored(self.TEST_TEXT, attrs="invalid")
            assert text == FMT_STR % (0, self.TEST_TEXT) + RESET

    def test_base_color_formatter(self):

        if is_above_python_3_2():
            with pytest.raises(ValueError):
                BaseColorFormatter(style="!")

        TEST_MESSAGE = "TestMessage"
        record = LogRecord(
            name="TestName",
            level=0,
            pathname="TestPathName",
            lineno=1,
            msg=TEST_MESSAGE,
            args=None,
            exc_info=None,
        )

        base_formatter = BaseColorFormatter()
        assert base_formatter.format(record) == TEST_MESSAGE + RESET

        base_formatter = BaseColorFormatter(fmt="TEST %(message)s")
        assert base_formatter.format(record) == "TEST " + TEST_MESSAGE + RESET

        colorfmt = {record.levelname: {"color": "black", "highlight": "red", "attribute": "bold"}}
        base_formatter = BaseColorFormatter(colorfmt=colorfmt)
        assert (
            base_formatter.format(record)
            == FMT_STR
            % (
                ATTRIBUTES["bold"],
                FMT_STR % (HIGHLIGHTS["red"], FMT_STR % (COLORS["black"], "TestMessage")),
            )
            + RESET
        )
