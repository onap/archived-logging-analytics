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
    from mock import patch, MagicMock
if sys.version_info[0] >= 3:
    from unittest.mock import patch, MagicMock

from onaplogging.utils import is_above_python_2_7, is_above_python_3_2


class TestUtils(unittest.TestCase):

    def test_is_above_python_3_2(self):
        with patch("onaplogging.utils.sys.version_info", (3, 4, 7)):
            assert is_above_python_3_2() is True

        with patch("onaplogging.utils.sys.version_info", (2, 7, 5)):
            assert is_above_python_3_2() is False

    def test_is_above_python_2_7(self):
        with patch("onaplogging.utils.sys.version_info", (3, 4, 7)):
            assert is_above_python_2_7() is True

        with patch("onaplogging.utils.sys.version_info", (2, 7, 5)):
            assert is_above_python_2_7() is True

        with patch("onaplogging.utils.sys.version_info", (2, 5, 6)):
            assert is_above_python_2_7() is False
