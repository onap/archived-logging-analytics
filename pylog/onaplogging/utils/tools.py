# Copyright (c) 2020 Deutsche Telekom.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

import yaml

from deprecated import deprecated


def yaml_to_dict(filepath):
    # type: (str) -> dict
    """YAML to Python dict converter.

    Args:
        filepath    : The filepath to a YAML file.
    Returns:
        dict        : Python dictionary object.
    """
    with open(filepath, 'rt') as f:
        return yaml.load(f.read())


@deprecated(reason="Will be removed. Call yaml_to_dict(filepath) instead.")
def _yaml2Dict(filename):
    """YAML to dict. See yaml_to_dict(filepath)."""
    return yaml_to_dict(filename)
