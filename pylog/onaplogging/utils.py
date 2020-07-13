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


def is_above_python_3_2():  # type: () -> bool
    """Check if code is running at least on Python 3.2 version.

    Returns:
        bool: True if it's at least 3.2 version, False otherwise

    """
    return sys.version_info >= (3, 2, 0, "final", 0)


def is_above_python_2_7():  # type: () -> bool
    """Check if code is running at least on Python 2.7 version.

    Returns:
        bool: True if it's at least 2.7 version, False otherwise

    """
    return sys.version_info >= (2, 7, 0, "final", 0)
