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
import threading
import io
import os
import traceback
import sys
import functools


__all__ = ['patch_loggingMDC', 'MDC']

_replace_func_name = ['info', 'critical', 'fatal', 'debug',
                      'error', 'warn', 'warning', 'log', 'findCaller']


class MDCContext(threading.local):
    """
    A Thread local instance to storage mdc values
    """
    def __init__(self):

        super(MDCContext, self).__init__()
        self._localDict = {}

    def get(self, key):

        return self._localDict.get(key, None)

    def put(self, key, value):

        self._localDict[key] = value

    def remove(self, key):

        if key in self.localDict:
            del self._localDict[key]

    def clear(self):

        self._localDict.clear()

    def result(self):

        return self._localDict

    def isEmpty(self):

        return self._localDict == {} or self._localDict is None


MDC = MDCContext()


def fetchkeys(func):

    @functools.wraps(func)
    def replace(*args, **kwargs):
        kwargs['extra'] = _getmdcs(extra=kwargs.get('extra', None))
        func(*args, **kwargs)
    return replace


def _getmdcs(extra=None):
    """
    Put mdc dict in logging record extra filed with key 'mdc'
    :param extra: dict
    :return: mdc dict
    """
    if MDC.isEmpty():
        return

    mdc = MDC.result()

    if extra is not None:
        for key in extra:
            #  make sure extra key dosen't override mdckey
            if key in mdc or key == 'mdc':
                    raise KeyError("Attempt to overwrite %r in MDC" % key)
    else:
        extra = {}

    extra['mdc'] = mdc
    del mdc
    return extra


@fetchkeys
def info(self, msg, *args, **kwargs):

    if self.isEnabledFor(logging.INFO):
        self._log(logging.INFO, msg, args, **kwargs)


@fetchkeys
def debug(self, msg, *args, **kwargs):

    if self.isEnabledFor(logging.DEBUG):
        self._log(logging.DEBUG, msg, args, **kwargs)


@fetchkeys
def warning(self, msg, *args, **kwargs):
    if self.isEnabledFor(logging.WARNING):
        self._log(logging.WARNING, msg, args, **kwargs)


@fetchkeys
def exception(self, msg, *args, **kwargs):

    kwargs['exc_info'] = 1
    self.error(msg, *args, **kwargs)


@fetchkeys
def critical(self, msg, *args, **kwargs):

    if self.isEnabledFor(logging.CRITICAL):
        self._log(logging.CRITICAL, msg, args, **kwargs)


@fetchkeys
def error(self, msg, *args, **kwargs):
    if self.isEnabledFor(logging.ERROR):
        self._log(logging.ERROR, msg, args, **kwargs)


@fetchkeys
def log(self, level, msg, *args, **kwargs):

    if not isinstance(level, int):
        if logging.raiseExceptions:
            raise TypeError("level must be an integer")
        else:
            return

    if self.isEnabledFor(level):
        self._log(level, msg, args, **kwargs)


def findCaller(self, stack_info=False):

    f = logging.currentframe()
    if f is not None:
        f = f.f_back
    rv = "(unkown file)", 0, "(unknow function)"
    while hasattr(f, "f_code"):
        co = f.f_code
        filename = os.path.normcase(co.co_filename)
        # jump through local 'replace' func frame
        if filename == logging._srcfile or co.co_name == "replace":
            f = f.f_back
            continue
        if sys.version_info > (3, 2):
            sinfo = None
            if stack_info:
                sio = io.StringIO()
                sio.write("Stack (most recent call last):\n")
                traceback.print_stack(f, file=sio)
                sinfo = sio.getvalue()
                if sinfo[-1] == '\n':
                    sinfo = sinfo[:-1]
                sio.close()
            rv = (co.co_filename, f.f_lineno, co.co_name, sinfo)
        else:
            rv = (co.co_filename, f.f_lineno, co.co_name)

        break

    return rv


def patch_loggingMDC():
    """
    The patch to add MDC ability in logging Record instance at runtime
    """
    localModule = sys.modules[__name__]
    for attr in dir(logging.Logger):
        if attr in _replace_func_name:
            newfunc = getattr(localModule, attr, None)
            if newfunc:
                setattr(logging.Logger, attr, newfunc)
