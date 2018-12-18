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
# LOG-905
# the manifest is manually maintained and does not drive the image tags in values.yaml
# prepull specific to casablanca - use the staging override for other branches
BRANCH=casablanca
sudo curl https://git.onap.org/integration/plain/version-manifest/src/main/resources/docker-manifest.csv?h=$BRANCH > docker-manifest-$BRANCH.csv
NEXUS3=nexus3.onap.org:10001
# login twice - the first one periodically times out
sudo docker login -u docker -p docker $NEXUS3
sudo docker login -u docker -p docker $NEXUS3

# this line from Gary Wu
for IMAGE_TAG in $(tail -n +2 docker-manifest-$BRANCH.csv | tr ',' ':'); do
    dt="$(date +"%T")"
    echo "$dt: pulling $IMAGE_TAG"
    sudo docker pull $NEXUS3/$IMAGE_TAG
done
