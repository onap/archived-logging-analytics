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

import os
import yaml
import traceback
from logging import config
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler


__all__ = ['patch_loggingYaml']


def _yaml2Dict(filename):

    with open(filename, 'rt') as f:
        return yaml.load(f.read())


class FileEventHandlers(FileSystemEventHandler):

    def __init__(self, filepath):

        FileSystemEventHandler.__init__(self)
        self.filepath = filepath
        self.currentConfig = None

    def on_modified(self, event):
        try:
            if event.src_path == self.filepath:
                newConfig = _yaml2Dict(self.filepath)
                print("reload logging configure file %s" % event.src_path)
                config.dictConfig(newConfig)
                self.currentConfig = newConfig

        except Exception:
            traceback.print_exc()
            print("Reuse the old configuration to avoid this"
                  "exception terminate program")
            if self.currentConfig:
                config.dictConfig(self.currentConfig)


def _yamlConfig(filepath=None, watchDog=None):

    """
    load logging configureation from yaml file and monitor file status

    :param filepath: logging yaml configure file absolute path
    :param watchDog: monitor yaml file identifier status
    :return:
    """
    if os.path.isfile(filepath) is False:
        raise OSError("wrong file")

    dirpath = os.path.dirname(filepath)
    event_handler = None

    try:
        dictConfig = _yaml2Dict(filepath)
        #  The watchdog could monitor yaml file status,if be modified
        #  will send a notify  then we could reload logging configuration
        if watchDog:
            observer = Observer()
            event_handler = FileEventHandlers(filepath)
            observer.schedule(event_handler=event_handler, path=dirpath,
                              recursive=False)
            observer.setDaemon(True)
            observer.start()

        config.dictConfig(dictConfig)

        if event_handler:
            # here we keep the correct configuration for reusing
            event_handler.currentConfig = dictConfig

    except Exception:
        traceback.print_exc()


def patch_loggingYaml():

    # The patch to add yam config forlogginf and runtime
    # reload logging when modify yaml file
    config.yamlConfig = _yamlConfig
