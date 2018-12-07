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
#
# This installation is for a rancher managed install of kubernetes
# after this run the standard oom install
# this installation can be run on amy ubuntu 16.04 VM or physical host
# https://wiki.onap.org/display/DW/ONAP+on+Kubernetes
# source from https://jira.onap.org/browse/LOG-325
# Michael O'Brien
#
# master/dublin - LOG-895
#     Rancher 1.6.25, Kubernetes 1.11.3, kubectl 1.11.3, Helm 2.9.2, Docker 17.03
# run as root - because of the logout that would be required after the docker user set

usage() {
cat <<EOF
Usage: $0 [PARAMs]
-u                  : Display usage
-b [branch]         : branch = master or amsterdam (required)
-s [server]         : server = IP or DNS name (required)
-e [environment]    : use the default (onap)
EOF
}

install_rancher_and_onap() {
  echo "Running rancher install first - 25 min"
  wget https://git.onap.org/logging-analytics/plain/deploy/rancher/oom_rancher_setup.sh
  sudo chmod 777 oom_rancher_setup.sh
  sudo ./oom_rancher_setup.sh -b $BRANCH -s $SERVER -e $ENVIRON
  echo "Running oom install - 45-90 min - 120 pod limit per vm"
  wget https://git.onap.org/logging-analytics/plain/deploy/cd.sh
  sudo chmod 777 cd.sh
  wget https://jira.onap.org/secure/attachment/11124/aaiapisimpledemoopenecomporg.cer
  if [ "$BRANCH" == "amsterdam" ]; then
    wget https://jira.onap.org/secure/attachment/11218/onap-parameters-amsterdam.yaml
    sudo cp onap-parameters-amsterdam.yaml onap-parameters.yaml
  else
    wget https://jira.onap.org/secure/attachment/11414/values.yaml
  fi
  wget https://jira.onap.org/secure/attachment/11126/aai-cloud-region-put.json
  sudo ./cd.sh -b $BRANCH -e $ENVIRON
}

BRANCH=master
SERVER=
ENVIRON=onap

while getopts ":b:s:e:u:" PARAM; do
  case $PARAM in
    u)
      usage
      exit 1
      ;;
    b)
      BRANCH=${OPTARG}
      ;;
    e)
      ENVIRON=${OPTARG}
      ;;
    s)
      SERVER=${OPTARG}
      ;;
    ?)
      usage
      exit
      ;;
  esac
done

if [[ -z $SERVER ]]; then
  usage
  exit 1
fi

install_rancher_and_onap $BRANCH $SERVER $ENVIRON
