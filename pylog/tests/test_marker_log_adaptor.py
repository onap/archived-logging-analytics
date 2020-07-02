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

from onaplogging.marker import BaseMarker
from onaplogging.markerLogAdaptor import MarkerLogAdaptor


class TestMarkerLogAdaptor(unittest.TestCase):

    def test_process(self):
        log_adaptor = MarkerLogAdaptor(MagicMock(), extra=None)
        msg, kwargs = log_adaptor.process("test", {})
        self.assertEqual(msg, "test")
        self.assertDictEqual(kwargs, {"extra": None})

        log_adaptor = MarkerLogAdaptor(MagicMock(), extra={"A": "B"})
        msg, kwargs = log_adaptor.process("test", {})
        self.assertEqual(msg, "test")
        self.assertDictEqual(kwargs, {"extra": {"A": "B"}})

    # Commented out due to that: https://bugs.python.org/issue20239
    # Comment out if Jenkis build runs using Python > 3.6
    # def test_markers(self):
    #     log_adaptor = MarkerLogAdaptor(MagicMock(), extra=None)

    #     with patch("onaplogging.markerLogAdaptor.LoggerAdapter.info") as mock_info:
    #         log_adaptor.infoMarker(BaseMarker("info_marker"), "test_message")
    #         mock_info.assert_called_once()

    #     with patch("onaplogging.markerLogAdaptor.LoggerAdapter.debug") as mock_debug:
    #         log_adaptor.debugMarker(BaseMarker("info_marker"), "test_message")
    #         mock_debug.assert_called_once()

    #     with patch("onaplogging.markerLogAdaptor.LoggerAdapter.warning") as mock_warning:
    #         log_adaptor.warningMarker(BaseMarker("info_marker"), "test_message")
    #         mock_warning.assert_called_once()

    #     with patch("onaplogging.markerLogAdaptor.LoggerAdapter.error") as mock_error:
    #         log_adaptor.errorMarker(BaseMarker("info_marker"), "test_message")
    #         mock_error.assert_called_once()

    #     with patch("onaplogging.markerLogAdaptor.LoggerAdapter.exception") as mock_exception:
    #         log_adaptor.exceptionMarker(BaseMarker("info_marker"), "test_message")
    #         mock_exception.assert_called_once()

    #     with patch("onaplogging.markerLogAdaptor.LoggerAdapter.critical") as mock_critical:
    #         log_adaptor.criticalMarker(BaseMarker("info_marker"), "test_message")
    #         mock_critical.assert_called_once()

    #     with patch("onaplogging.markerLogAdaptor.LoggerAdapter.log") as mock_log:
    #         log_adaptor.logMarker(BaseMarker("info_marker"), "info", "test_message")
    #         mock_log.assert_called_once()

    #     with pytest.raises(TypeError):
    #         log_adaptor.infoMarker("info_marker_str", "test_message")

    #     with pytest.raises(Exception):
    #         log_adaptor = MarkerLogAdaptor(MagicMock(), extra={"marker": "exception"})
    #         log_adaptor.infoMarker(BaseMarker("info_marker"), "test_message")
