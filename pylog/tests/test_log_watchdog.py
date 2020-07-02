# Copyright (c) 2020 Deutsche Telekom.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

import os
import sys
import unittest
from collections import namedtuple
from tempfile import NamedTemporaryFile

if sys.version_info[0] < 3:
    from mock import patch
if sys.version_info[0] >= 3:
    from unittest.mock import patch

import pytest
import yaml

from onaplogging.logWatchDog import FileEventHandlers, _yaml2Dict, _yamlConfig


TestEvent = namedtuple("TestEvent", ["src_path"])


class TestLogWatchdog(unittest.TestCase):

    TEST_DICT = {
        "A": {
            "B": "C"
        }
    }

    def setUp(self):
        super(TestLogWatchdog, self).setUp()
    
        self.temp_file = NamedTemporaryFile(mode="w+t", delete=False)
        self.temp_file.write(yaml.dump(self.TEST_DICT))
        self.temp_file.close()

    def tearDown(self):
        super(TestLogWatchdog, self).tearDown()

        os.unlink(self.temp_file.name)

    def test_yaml2dict(self):
        with pytest.raises(TypeError):
            _yaml2Dict(None)
        
        self.assertDictEqual(self.TEST_DICT, _yaml2Dict(self.temp_file.name))

    def test_file_event_handler(self):

        with patch("onaplogging.logWatchDog.config.dictConfig") as mock_config:
            mock_config.side_effect = Exception

            feh = FileEventHandlers(self.temp_file.name)
            self.assertIsNone(feh.currentConfig)
            feh.on_modified(TestEvent(src_path=self.temp_file.name))
            self.assertIsNone(feh.currentConfig)

        with patch("onaplogging.logWatchDog.config"):

            feh = FileEventHandlers(self.temp_file.name)
            self.assertIsNone(feh.currentConfig)
            feh.on_modified(TestEvent(src_path=self.temp_file.name))
            self.assertIsNotNone(feh.currentConfig)

    def test_patch_yaml_config(self):

        with pytest.raises(TypeError):
            _yamlConfig(filepath=None)

        with pytest.raises(OSError):
            _yamlConfig(filepath="invalid path")

        with patch("onaplogging.logWatchDog.config.dictConfig") as mock_config:
            _yamlConfig(filepath=self.temp_file.name)
            mock_config.assert_called_once_with(self.TEST_DICT)

        with patch("onaplogging.logWatchDog.config.dictConfig") as mock_config:
            with patch("onaplogging.logWatchDog.Observer.start") as mock_observer_start:
                _yamlConfig(filepath=self.temp_file.name, watchDog=True)
                mock_config.assert_called_once_with(self.TEST_DICT)
                mock_observer_start.assert_called_once()
