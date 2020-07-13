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

from onaplogging.marker import BaseMarker, matchMarkerHelp, MarkerFactory, MarkerFilter, MarkerNotifyHandler


class TestRecordMixin(object):

    Record = namedtuple("Record", "marker")


class TestNameMixin(object):

    TEST_NAME = "test_base"


class TestBaseMarker(unittest.TestCase, TestNameMixin):

    def setUp(self):
        super(TestBaseMarker, self).setUp()
        self.base_marker = BaseMarker(name=self.TEST_NAME)

    def test_base_marker_name(self):
        with pytest.raises(TypeError):
            BaseMarker(123)

        with pytest.raises(ValueError):
            BaseMarker(name="")

        self.assertEqual(self.base_marker.getName(), self.TEST_NAME)

    def test_base_marker_contains(self):
        self.assertTrue(self.base_marker.contains(self.base_marker))
        self.assertTrue(self.base_marker.contains(self.TEST_NAME))

    def test_base_marker_compare(self):
        self.assertNotEqual(self.base_marker, 3)
        self.assertEqual(self.base_marker, self.base_marker)
        other = BaseMarker("Other")
        self.assertNotEqual(self.base_marker, other)
        other = BaseMarker(self.TEST_NAME)
        self.assertEqual(self.base_marker, other)

    def test_base_marker_child(self):
        self.assertListEqual(list(iter(self.base_marker)), [])
        self.assertFalse(self.base_marker.contains(3))
        with pytest.raises(TypeError):
            self.base_marker.addChild(3)
        with pytest.raises(TypeError):
            self.base_marker.addChild("str")
        with pytest.raises(TypeError):
            self.base_marker.removeChild(3)

        self.base_marker.addChild(self.base_marker)
        self.assertListEqual(list(iter(self.base_marker)), [])

        child1 = BaseMarker(name="child1")
        self.assertFalse(self.base_marker.contains(child1))
        self.base_marker.addChild(child1)
        self.assertListEqual(list(iter(self.base_marker)), [child1])
        self.assertTrue(self.base_marker.contains(child1))
        self.base_marker.addChild(child1)
        self.assertListEqual(list(iter(self.base_marker)), [child1])

        self.base_marker.removeChild(child1)
        self.assertListEqual(list(iter(self.base_marker)), [])
        self.assertFalse(self.base_marker.contains(child1))

        child2 = BaseMarker(name="child2")
        self.assertFalse(self.base_marker.contains(child2))

        with pytest.raises(TypeError):
            self.base_marker.addChilds(None)
        self.base_marker.addChilds((child1, child2,))
        self.assertTrue(self.base_marker.contains(child1))
        self.assertTrue(self.base_marker.contains(child2))
        self.base_marker.removeChild(child1)
        self.assertFalse(self.base_marker.contains(child1))
        self.assertTrue(self.base_marker.contains(child2))
        self.assertFalse(self.base_marker.contains("child1"))
        self.assertTrue(self.base_marker.contains("child2"))
        

class TestMatchMarkerHelp(unittest.TestCase, TestRecordMixin, TestNameMixin):
    CHILD_NAME = "child"

    def test_match_marker_help(self):
        record = self.Record(None)
        self.assertFalse(matchMarkerHelp(record, "anything"))

        record = self.Record("not_marker_instance")
        self.assertFalse(matchMarkerHelp(record, "not_marker_instance"))

        marker = BaseMarker(self.TEST_NAME)
        record = self.Record(marker)
        self.assertFalse(matchMarkerHelp(record, "invalid_name"))
        self.assertTrue(matchMarkerHelp(record, marker))
        self.assertTrue(matchMarkerHelp(record, self.TEST_NAME))
    
        child = BaseMarker(self.CHILD_NAME)
        marker.addChild(child)
        self.assertTrue(matchMarkerHelp(record, [self.TEST_NAME, self.CHILD_NAME]))
        self.assertTrue(matchMarkerHelp(record, [marker, self.CHILD_NAME]))
        self.assertTrue(matchMarkerHelp(record, [marker, child]))
        self.assertTrue(matchMarkerHelp(record, [marker, "invalid"]))


class TestMarkerFactory(unittest.TestCase, TestNameMixin):

    def setUp(self):
        super(TestMarkerFactory, self).setUp()
        self.marker_factory = MarkerFactory()
    
    def test_get_marker(self):
        with pytest.raises(ValueError):
            self.marker_factory.getMarker()
        self.assertEqual(len(self.marker_factory._marker_map), 0)
        marker = self.marker_factory.getMarker(self.TEST_NAME)
        self.assertEqual(marker.getName(), self.TEST_NAME)
        self.assertEqual(len(self.marker_factory._marker_map), 1)
        marker = self.marker_factory.getMarker(self.TEST_NAME)
        self.assertEqual(marker.getName(), self.TEST_NAME)
        self.assertEqual(len(self.marker_factory._marker_map), 1)

        self.assertTrue(self.marker_factory.exist(marker.getName()))
    
        self.assertTrue(self.marker_factory.deleteMarker(marker.getName()))
        self.assertFalse(self.marker_factory.exist(marker.getName()))
        self.assertEqual(len(self.marker_factory._marker_map), 0)

        self.assertFalse(self.marker_factory.deleteMarker(marker.getName()))


class TestMarkerFilter(unittest.TestCase, TestRecordMixin, TestNameMixin):

    def test_marker_filter(self):
        marker_filter = MarkerFilter()

        record = self.Record(BaseMarker(self.TEST_NAME))
        self.assertFalse(marker_filter.filter(record))

        marker_filter = MarkerFilter(markers=BaseMarker(self.TEST_NAME))
        self.assertTrue(marker_filter.filter(record))


class TestMarkerNotifyHandler(unittest.TestCase, TestRecordMixin, TestNameMixin):

    def test_marker_notify_handler(self):
        record = self.Record(BaseMarker(self.TEST_NAME))

        notify_handler = MarkerNotifyHandler("test_host", "fromaddr", "toaddrs", "subject")
        self.assertIsNone(notify_handler.markers)
        self.assertFalse(notify_handler.handle(record))

        marker = BaseMarker(self.TEST_NAME)
        notify_handler = MarkerNotifyHandler("test_host", "fromaddr", "toaddrs", "subject", markers=[marker])
        with patch("onaplogging.marker.markerHandler.SMTPHandler.handle") as mock_smtp_handler_handle:
            mock_smtp_handler_handle.return_value = True
            self.assertTrue(notify_handler.handle(record))
        record = self.Record(BaseMarker("other"))
        self.assertFalse(notify_handler.handle(record))
