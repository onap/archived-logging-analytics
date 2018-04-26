#!/bin/bash
#############################################################################
#
# Copyright © 2018 Amdocs.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#############################################################################
# v20180625
# https://wiki.onap.org/display/DW/Cloud+Native+Deployment
# source from https://jira.onap.org/browse/LOG-137
# Michael O'Brien


#mkdir target
cp ../../logging-demo/target/*.war target
docker build -t oomk8s/logging-demo-nbi -f DockerFile .
docker images | grep logging-demo-nbi
docker tag oomk8s/logging-demo-nbi oomk8s/logging-demo-nbi:0.0.1
docker login
docker push oomk8s/logging-demo-nbi:0.0.1
#docker run -d -it --rm -p 8888:8080 oomk8s/logging-demo-nbi:latest
