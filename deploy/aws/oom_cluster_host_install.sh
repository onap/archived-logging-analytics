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
# v20180618
# Amazon AWS specific EFS/NFS share and rancher host join script for each cluster node
# https://wiki.onap.org/display/DW/Cloud+Native+Deployment
# source from https://jira.onap.org/browse/OOM-320
# Michael O'Brien

usage() {
cat <<EOF
Usage: $0 [PARAMs]
example
 master
 sudo ./cluster.sh -n false -s cl.onap.info -e fs-00c0f00 -r us-east-2
 clients
 sudo ./cluster.sh -n true -s cl.onap.info -e fs-00c0f00 -r us-east-2 -t 23D4:15:qQ -c false -a host1.onap.info -v true
 sudo ./oom_cluster_host_install.sh -n true -s cd.onap.cloud -e 1 -r us-east-2 -t token -c false -a 104.209.158.156 -v true
Prereq:
  You must create an EFS (NFS wrapper) on AWS

-u                      : Display usage
-n [true|false]         : is node (non master)
-s [master server]      : target server (ip or FQDN only)
-e [AWS efs id]         : AWS Elastic File System ID prefix
-r [AWS region prefix]  : AWS Region prefix
-t [token]              : registration token
-c [true/false]         : use computed client address
-a [IP address]         : client address ip - no FQDN
-v [validate true/false]: optional
EOF
}

register_node() {
    #constants
    DOCKERDATA_NFS=dockerdata-nfs
    DOCKER_VER=17.03
    USERNAME=ubuntu

    if [[ "$IS_NODE" != false ]]; then
        sudo curl https://releases.rancher.com/install-docker/$DOCKER_VER.sh | sh
        sudo usermod -aG docker $USERNAME
    fi
    sudo apt-get install nfs-common -y
    sudo mkdir /$DOCKERDATA_NFS
    sudo mount -t nfs4 -o nfsvers=4.1,rsize=1048576,wsize=1048576,hard,timeo=600,retrans=2 $AWS_EFS.efs.$AWS_REGION.amazonaws.com:/ /$DOCKERDATA_NFS
    if [[ "$IS_NODE" != false ]]; then
        echo "Running agent docker..."
        if [[ "$COMPUTEADDRESS" != false ]]; then
            echo "sudo docker run --rm --privileged -v /var/run/docker.sock:/var/run/docker.sock -v /var/lib/rancher:/var/lib/rancher rancher/agent:v1.2.9 http://$MASTER:8880/v1/scripts/$TOKEN"
            sudo docker run --rm --privileged -v /var/run/docker.sock:/var/run/docker.sock -v /var/lib/rancher:/var/lib/rancher rancher/agent:v1.2.9 http://$MASTER:8880/v1/scripts/$TOKEN
        else
            echo "sudo docker run -e CATTLE_AGENT_IP=\"$ADDRESS\" --rm --privileged -v /var/run/docker.sock:/var/run/docker.sock -v /var/lib/rancher:/var/lib/rancher rancher/agent:v1.2.9 http://$MASTER:8880/v1/scripts/$TOKEN"
            sudo docker run -e CATTLE_AGENT_IP="$ADDRESS" --rm --privileged -v /var/run/docker.sock:/var/run/docker.sock -v /var/lib/rancher:/var/lib/rancher rancher/agent:v1.2.9 http://$MASTER:8880/v1/scripts/$TOKEN
        fi
    fi
}

IS_NODE=false
MASTER=
TOKEN=
AWS_REGION=
AWS_EFS=
COMPUTEADDRESS=true
ADDRESS=
VALIDATE=
while getopts ":u:n:s:e:r:t:c:a:v" PARAM; do
  case $PARAM in
    u)
      usage
      exit 1
      ;;
    n)
      IS_NODE=${OPTARG}
      ;;
    s)
      MASTER=${OPTARG}
      ;;
    e)
      AWS_EFS=${OPTARG}
      ;;
    r)
      AWS_REGION=${OPTARG}
      ;;
    t)
      TOKEN=${OPTARG}
      ;;
    c)
      COMPUTEADDRESS=${OPTARG}
      ;;
    a)
      ADDRESS=${OPTARG}
      ;;
    v)
      VALIDATE=${OPTARG}
      ;;
    ?)
      usage
      exit
      ;;
  esac
done

if [ -z $MASTER ]; then
  usage
  exit 1
fi

register_node  $IS_NODE $MASTER $AWS_EFS $AWS_REGION $TOKEN $COMPUTEADDRESS $ADDRESS $VALIDATE
echo "if you get ERROR: http://$MASTER:8880/v1 is not accessible (The requested URL returned error: 404 Not Found) - check your token"
printf "**** Done ****\n"
