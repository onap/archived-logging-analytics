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
    long_description=open('README.md').read(),
    version="1.0.5",
    license="MIT Licence",
    author='ke liang',
    author_email="lokyse@163.com",
    packages=find_packages(),
    platforms=['all'],
    install_requires=[
        "PyYAML>=3.10",
        "watchdog>=0.8.3"
    ],
    classifiers=[
        'Development Status :: 4 - Beta',
        'Intended Audience :: Developers',
        'Programming Language :: Python :: 2.7'
    ]
)
