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
import traceback

from logging import config
from typing import Dict, Optional, Any
from deprecated import deprecated
from warnings import warn

from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler, FileSystemEvent

from onaplogging.utils.tools import yaml_to_dict


__all__ = ['patch_loggingYaml']  # rename after the deprecated name changed


class FileEventHandlers(FileSystemEventHandler):
    """Handler of the events in the file system.

    Use it to keep and eye on files in the file system.

    Extends:
        watchdog.events.FileSystemEventHandler
    Properties:
        filepath        : The path to the file to be monitored.
        current_config  : Defaults to None.
    Args:
        filepath        : The path to the file to be monitored.
    """

    @property
    def filepath(self):
        # type: () -> str
        return self._filepath

    @property
    def current_config(self):
        # type: () -> str
        return self.currentConfig  # deprecated, replace with _current_config

    @filepath.setter
    def filepath(self, value):
        # type: (str) -> str
        self._filepath = value

    @current_config.setter
    def current_config(self, value):
        # type: (Dict) -> Dict
        self.currentConfig = value

    def __init__(self, filepath):  # type: (str)
        warn("Attribute currentConfig will be replaced with property"
               "current_config. Use current_config instead.")

        FileSystemEventHandler.__init__(self)

        self.filepath = filepath
        self.current_config = None

    def on_modified(self, event):
        # type: (FileSystemEvent) -> None
        """Configuration file actualizer.

        When an event occurs in the file system  the hadnler's filepath
        is taken to update the configuration file. If the actualization
        of  the  config file  fails it  will  keep the old  config file.

        Args:
            event       : Represents an event on the file system.
        Raises:
            Exception   : If the actualization of the config file fails.
        """
        try:
            if event.src_path == self.filepath:

                new_config = yaml_to_dict(self.filepath)
                print("Reloading logging configuration file %s "
                        % event.src_path)

                config.dictConfig(new_config)
                self.current_config = new_config

        except Exception:
            traceback.print_exc()
            print("Reuse the old configuration to avoid this"
                  "exception terminate program")

            if self.current_config:
                config.dictConfig(self.current_config)


def _yamlConfig(filepath=None, watchDog=None):
    # type: (Optional[str], Optional[Any]) -> None
    """YAML configuration file loader.

    Use it to monitor a file status in a directory.  The watchdog can monitor
    a YAML file status looking for modifications. If the watchdog is provided
    start  observing  the  directory. The new configuration  file is saved as
    current for the later reuse.

    Args:
        filepath    : The path to the file to be monitored.   Defaults to None.
        watchDog    : Monitors a YAML file identifier status. Defaults to None.

    Raises:
        OSError     : If the requested file in the filepath is not a file.
        Exception   : If watchdog observer setup  or YAML coversion fails.
    """

    is_file = os.path.isfile(filepath)

    if is_file is False:
        raise OSError("%s is not a file" % (filepath))

    dirpath = os.path.dirname(filepath)
    event_handler = None

    try:
        dictConfig = yaml_to_dict(filepath)
        # Dev note: Will send a notify then we could reload logging config
        if watchDog:
            observer = Observer()
            event_handler = FileEventHandlers(filepath)
            observer.schedule(event_handler=event_handler,
                              path=dirpath,
                              recursive=False)
            observer.setDaemon(True)
            observer.start()

        config.dictConfig(dictConfig)

        if event_handler:
            event_handler.currentConfig = dictConfig

    except Exception:
        traceback.print_exc()


def patch_logging_yaml():
    # type: () -> None
    """YAML configuration patch.

    Adds the YAML configuration file loader
    to logging.config module during runtime.
    """
    config.yamlConfig = _yamlConfig


@deprecated(reason="Will be removed. Call patch_logging_yaml() instead.")
def patch_loggingYaml():
    """See patch_logging_yaml()"""
    patch_logging_yaml()
