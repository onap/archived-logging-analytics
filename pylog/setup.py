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


from setuptools import setup, find_packages

setup(

    name='onappylog',
    keywords=("yaml", "logging", "mdc", "onap"),
    description='onap python logging library',
    long_description="python-package onappylog could be used in any python"
                     "project to record MDC information and reload logging"
                     "at runtime",
    version="1.0.7",
    license="Apache 2.0",
    author='ke liang',
    author_email="lokyse@163.com",
    packages=find_packages(),
    platforms=['all'],
    url="https://github.com/onap/logging-analytics/tree/master/pylog",
    install_requires=[
        "PyYAML>=3.10",
        "watchdog>=0.8.3"
    ],
    classifiers=[
        'Development Status :: 4 - Beta',
        'Intended Audience :: Developers',
        'Programming Language :: Python :: 2.7',
        'Programming Language :: Python :: 3'
    ]
)
