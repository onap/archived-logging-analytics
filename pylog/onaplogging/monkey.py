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


from mdcContext import patch_loggingMDC
from logWatchDog import patch_loggingYaml


__all__ = ["patch_all"]


def patch_all(mdc=True, yaml=True):

    if mdc is True:
        patch_loggingMDC()

    if yaml is True:
        patch_loggingYaml()
