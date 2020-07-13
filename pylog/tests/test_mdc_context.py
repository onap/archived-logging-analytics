# Copyright (c) 2020 Deutsche Telekom.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

import logging
import sys
import unittest

if sys.version_info[0] < 3:
    from mock import MagicMock, patch
if sys.version_info[0] >= 3:
    from unittest.mock import MagicMock, patch

import pytest

from onaplogging.mdcContext import (
    _getmdcs, 
    MDCContext, 
    info, 
    debug,
    warning, 
    exception, 
    critical, 
    error, 
    log,
    handle
)


class TestMDCContext(unittest.TestCase):

    def setUp(self):
        super(TestMDCContext, self).setUp()

        self.TEST_KEY = "key"
        self.TEST_VALUE = "value"
    
        self.mdc_context = MDCContext()

    def test_mdc_context(self):

        self.assertTrue(self.mdc_context.isEmpty())
        self.assertIsNone(self.mdc_context.get(self.TEST_KEY))
        self.mdc_context.remove(self.TEST_KEY)
        self.mdc_context.put(self.TEST_KEY, self.TEST_VALUE)
        self.assertFalse(self.mdc_context.isEmpty())
        self.assertEqual(self.mdc_context.get(self.TEST_KEY), self.TEST_VALUE)
        self.assertDictEqual(self.mdc_context.result(), {self.TEST_KEY: self.TEST_VALUE})
        self.mdc_context.remove(self.TEST_KEY)
        self.assertTrue(self.mdc_context.isEmpty())
        self.assertDictEqual(self.mdc_context.result(), {})
        self.mdc_context.put(self.TEST_KEY, self.TEST_VALUE)
        self.assertFalse(self.mdc_context.isEmpty())
        self.assertEqual(self.mdc_context.get(self.TEST_KEY), self.TEST_VALUE)
        self.assertDictEqual(self.mdc_context.result(), {self.TEST_KEY: self.TEST_VALUE})
        self.mdc_context.clear()
        self.assertTrue(self.mdc_context.isEmpty())
        self.assertDictEqual(self.mdc_context.result(), {})

    def test_getmdcs(self):
        with patch("onaplogging.mdcContext.MDC", self.mdc_context):
            self.assertIsNone(_getmdcs(None))
            self.mdc_context.put(self.TEST_KEY, self.TEST_VALUE)
            self.assertDictEqual(_getmdcs(None), {"mdc": {self.TEST_KEY: self.TEST_VALUE}})
            self.assertDictEqual(_getmdcs({"test": "value"}), {"mdc": {self.TEST_KEY: self.TEST_VALUE}, "test": "value"})
            with pytest.raises(KeyError):
                _getmdcs({self.TEST_KEY: self.TEST_VALUE})
            with pytest.raises(KeyError):
                _getmdcs({"mdc": "exception"})

    def test_fetchkeys_info(self):
        with patch("onaplogging.mdcContext.MDC", self.mdc_context):
            test_self = MagicMock()
            test_self.isEnabledFor.return_value = False
            info(test_self, "msg")
            test_self._log.assert_not_called()
            test_self.isEnabledFor.return_value = True
            info(test_self, "msg")
            test_self._log.assert_called_once_with(logging.INFO, "msg", (), extra=None)
            test_self._log.reset_mock()
            self.mdc_context.put(self.TEST_KEY, self.TEST_VALUE)
            info(test_self, "msg")
            test_self._log.assert_called_once_with(logging.INFO, "msg", (), extra={"mdc": {self.TEST_KEY: self.TEST_VALUE}})

    def test_fetchkeys_debug(self):
        with patch("onaplogging.mdcContext.MDC", self.mdc_context):
            test_self = MagicMock()
            test_self.isEnabledFor.return_value = False
            debug(test_self, "msg")
            test_self._log.assert_not_called()
            test_self.isEnabledFor.return_value = True
            debug(test_self, "msg")
            test_self._log.assert_called_once_with(logging.DEBUG, "msg", (), extra=None)
            test_self._log.reset_mock()
            self.mdc_context.put(self.TEST_KEY, self.TEST_VALUE)
            debug(test_self, "msg")
            test_self._log.assert_called_once_with(logging.DEBUG, "msg", (), extra={"mdc": {self.TEST_KEY: self.TEST_VALUE}})

    def test_fetchkeys_warning(self):
        with patch("onaplogging.mdcContext.MDC", self.mdc_context):
            test_self = MagicMock()
            test_self.isEnabledFor.return_value = False
            warning(test_self, "msg")
            test_self._log.assert_not_called()
            test_self.isEnabledFor.return_value = True
            warning(test_self, "msg")
            test_self._log.assert_called_once_with(logging.WARNING, "msg", (), extra=None)
            test_self._log.reset_mock()
            self.mdc_context.put(self.TEST_KEY, self.TEST_VALUE)
            warning(test_self, "msg")
            test_self._log.assert_called_once_with(logging.WARNING, "msg", (), extra={"mdc": {self.TEST_KEY: self.TEST_VALUE}})

    def test_fetchkeys_exception(self):
        with patch("onaplogging.mdcContext.MDC", self.mdc_context):
            test_self = MagicMock()
            test_self.isEnabledFor.return_value = False
            exception(test_self, "msg")
            test_self.error.assert_called_once_with("msg", exc_info=1, extra=None)

    def test_fetchkeys_critical(self):
        with patch("onaplogging.mdcContext.MDC", self.mdc_context):
            test_self = MagicMock()
            test_self.isEnabledFor.return_value = False
            critical(test_self, "msg")
            test_self._log.assert_not_called()
            test_self.isEnabledFor.return_value = True
            critical(test_self, "msg")
            test_self._log.assert_called_once_with(logging.CRITICAL, "msg", (), extra=None)
            test_self._log.reset_mock()
            self.mdc_context.put(self.TEST_KEY, self.TEST_VALUE)
            critical(test_self, "msg")
            test_self._log.assert_called_once_with(logging.CRITICAL, "msg", (), extra={"mdc": {self.TEST_KEY: self.TEST_VALUE}})

    def test_fetchkeys_error(self):
        with patch("onaplogging.mdcContext.MDC", self.mdc_context):
            test_self = MagicMock()
            test_self.isEnabledFor.return_value = False
            error(test_self, "msg")
            test_self._log.assert_not_called()
            test_self.isEnabledFor.return_value = True
            error(test_self, "msg")
            test_self._log.assert_called_once_with(logging.ERROR, "msg", (), extra=None)
            test_self._log.reset_mock()
            self.mdc_context.put(self.TEST_KEY, self.TEST_VALUE)
            error(test_self, "msg")
            test_self._log.assert_called_once_with(logging.ERROR, "msg", (), extra={"mdc": {self.TEST_KEY: self.TEST_VALUE}})

    def test_fetchkeys_log(self):
        with patch("onaplogging.mdcContext.MDC", self.mdc_context):
            test_self = MagicMock()
            test_self.isEnabledFor.return_value = False
            logging.raiseExceptions = False
            log(test_self, "invalid_level", "msg")
            logging.raiseExceptions = True
            with pytest.raises(TypeError):
                log(test_self, "invalid_level", "msg")
            log(test_self, logging.DEBUG, "msg")
            test_self._log.assert_not_called()
            test_self.isEnabledFor.return_value = True
            log(test_self, logging.DEBUG, "msg")
            test_self._log.assert_called_once()

    def test_handle(self):
        with patch("onaplogging.mdcContext.MDC", self.mdc_context):
            test_self = MagicMock()
            record = MagicMock()
            test_self.disabled = True
            test_self.filter.return_value = False
            handle(test_self, record)
            test_self.callHandlers.assert_not_called()

            test_self.disabled = False
            test_self.filter.return_value = False
            handle(test_self, record)
            test_self.callHandlers.assert_not_called()
            test_self.filter.assert_called_once_with(record)

            test_self.filter.reset_mock()
            test_self.disabled = False
            test_self.filter.return_value = True
            handle(test_self, record)
            test_self.callHandlers.assert_called_once()
            test_self.filter.assert_called_once_with(record)
