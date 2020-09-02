# Copyright 2018 ke liang <lokyse@163.com>.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os
import yaml
import traceback
from logging import config
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler


__all__ = ['patch_loggingYaml']


def _yaml2Dict(filename):
    """YAML to Python dict converter.

    Args:
        filename (str): The filepath of a YAML file.

    Returns:
        dict: Python dictionary object.
    """
    with open(filename, 'rt') as f:
        return yaml.load(f.read())


class FileEventHandlers(FileSystemEventHandler):
    """Handler of the events in the file system.

    Use it to keep and eye on files in the file system.

    Extends:
        watchdog.events.FileSystemEventHandler

    Attributes:
        filepath (str): the path to the file to be monitored.
        currentConfig (dict): Defaults to None.

    Args:
        filepath (str): the path to the file to be monitored.
    """

    def __init__(self, filepath):

        FileSystemEventHandler.__init__(self)
        self.filepath = filepath
        self.currentConfig = None

    def on_modified(self, event):
        """Config file actualizer

        When an event occurs in the file system the hadnler's filepath
        is taken to update the configuration file. If the actualization
        of the config file fails it will keep the old config file.

        Args:
            event (FileSystemEvent): Represents an event on the file system.

        Raises:
            Exception: if the actualization of a config file fails.
        """
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
    """YAML configuration file loader.

    Use it to monitor a file status in a directory. The watchdog can monitor
    a YAML file status looking for modifications. If the watchdog is provided
    start observing the directory. The new configuration file is saved as
    current for the later reuse.

    Args:
        filepath (str, optional): The path to the file to be monitored.
                                     Defaults to None.
        watchDog ([type]], optional): Monitors a YAML file identifier status.
                                     Defaults to None.

    Raises:
        OSError: if the requested file in the filepath is not a file.
        Exception: if watchdog observer setup or YAML coversion fails.
    """
    if os.path.isfile(filepath) is False:
        raise OSError("%s is not a file" % (filepath))

    dirpath = os.path.dirname(filepath)
    event_handler = None

    try:
        dictConfig = _yaml2Dict(filepath)
        # Dev note: Will send a notify then we could reload logging
        # configuration
        if watchDog:
            observer = Observer()
            event_handler = FileEventHandlers(filepath)
            observer.schedule(event_handler=event_handler, path=dirpath,
                              recursive=False)
            observer.setDaemon(True)
            observer.start()

        config.dictConfig(dictConfig)

        if event_handler:
            event_handler.currentConfig = dictConfig

    except Exception:
        traceback.print_exc()


def patch_loggingYaml():
    """YAML configuration patch.

    Adds the YAML configuration file loader
    to logging.config module during runtime.
    """
    config.yamlConfig = _yamlConfig
