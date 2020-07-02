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
    from mock import patch
if sys.version_info[0] >= 3:
    from unittest.mock import patch

from onaplogging.monkey import patch_all, patch_loggingMDC, patch_loggingYaml


class TestMonkey(unittest.TestCase):

    def test_patch_all(self):
        with patch("onaplogging.monkey.patch_loggingMDC") as mock_mdc:
            with patch("onaplogging.monkey.patch_loggingYaml") as mock_yaml:
                patch_all()
                mock_mdc.assert_called_once()
                mock_yaml.assert_called_once()

        with patch("onaplogging.monkey.patch_loggingMDC") as mock_mdc:
            with patch("onaplogging.monkey.patch_loggingYaml") as mock_yaml:
                patch_all(mdc=False)
                mock_mdc.assert_not_called()
                mock_yaml.assert_called_once()

        with patch("onaplogging.monkey.patch_loggingMDC") as mock_mdc:
            with patch("onaplogging.monkey.patch_loggingYaml") as mock_yaml:
                patch_all(yaml=False)
                mock_mdc.assert_called_once()
                mock_yaml.assert_not_called()

        with patch("onaplogging.monkey.patch_loggingMDC") as mock_mdc:
            with patch("onaplogging.monkey.patch_loggingYaml") as mock_yaml:
                patch_all(mdc=False, yaml=False)
                mock_mdc.assert_not_called()
                mock_yaml.assert_not_called()
