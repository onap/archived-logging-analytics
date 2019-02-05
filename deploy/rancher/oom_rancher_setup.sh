#!/bin/bash
#############################################################################
#
# Copyright © 2019 Amdocs.
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
# this installation can be run on amy ubuntu 16.04 VM, RHEL 7.6 (root only), physical or cloud azure/aws host
# https://wiki.onap.org/display/DW/Cloud+Native+Deployment
# source from https://jira.onap.org/browse/LOG-320
# Michael O'Brien
# amsterdam
#     Rancher 1.6.10, Kubernetes 1.7.7, Kubectl 1.7.7, Helm 2.3.0, Docker 1.12
# beijing
#     Rancher 1.6.14, Kubernetes 1.8.10, Kubectl 1.8.10, Helm 2.8.2, Docker 17.03
# casablanca (until RC1)
#     Rancher 1.6.18, Kubernetes 1.10.3, Kubectl 1.10.3, Helm 2.9.2, Docker 17.03
# casablanca - integration change alignment for INT-586 - 29th Oct via LOG-806
#     Rancher 1.6.22, Kubernetes 1.11.5, kubectl 1.11.5, Helm 2.9.1, Docker 17.03
# master/dublin - LOG-895
#     Rancher 1.6.25, Kubernetes 1.11.5, kubectl 1.11.5, Helm 2.9.1, Docker 17.03

usage() {
cat <<EOF
Usage: $0 [PARAMs]
example
sudo ./oom_rancher_setup.sh -b master -s cd.onap.cloud -e onap -c false -a 104.209.168.116 -l ubuntu -v true
-u                  : Display usage
-b [branch]         : branch = master or beijing or amsterdam (required)
-s [server]         : server = IP or DNS name (required)
-e [environment]    : use the default (onap)
-c [true/false]     : use computed client address (default true)
-a [IP address]     : client address ip - no FQDN
-l [username]       : login username account (use ubuntu and sudo for ubuntu, use root for RHEL)
-v [true/false]     : validate
EOF
}

install_onap() {
  #constants
  PORT=8880
  if [ "$BRANCH" == "amsterdam" ]; then
    RANCHER_VERSION=1.6.10
    KUBECTL_VERSION=1.7.7
    HELM_VERSION=2.3.0
    DOCKER_VERSION=1.12
    AGENT_VERSION=1.2.6
  elif [ "$BRANCH" == "beijing" ]; then
    RANCHER_VERSION=1.6.14
    KUBECTL_VERSION=1.8.10
    HELM_VERSION=2.8.2
    DOCKER_VERSION=17.03
    AGENT_VERSION=1.2.9
  elif [ "$BRANCH" == "casablanca" ]; then
    RANCHER_VERSION=1.6.22
    KUBECTL_VERSION=1.11.5
    HELM_VERSION=2.9.1
    DOCKER_VERSION=17.03
    AGENT_VERSION=1.2.11
  else
    RANCHER_VERSION=1.6.25
    KUBECTL_VERSION=1.11.5
    HELM_VERSION=2.9.1
    DOCKER_VERSION=17.03
    AGENT_VERSION=1.2.11
  fi
  echo "prep for RHEL 7.6"
  echo "enable ipv4 forwarding - add to /etc/sysctl.conf - net.ipv4.ip_forward = 1"
  echo "yum groupinstall Development Tools - last 2 in single quotes"
  echo "disable the firewall - systemctl disable firewalld"
  echo "verify networking is boot enabled - sudo vi /etc/sysconfig/network-scripts/ifcfg-ens33 with ONBOOT=yes"
  echo "Installing on ${SERVER} for ${BRANCH}: Rancher: ${RANCHER_VERSION} Kubectl: ${KUBECTL_VERSION} Helm: ${HELM_VERSION} Docker: ${DOCKER_VERSION} username: ${USERNAME}"
  sudo echo "127.0.0.1 ${SERVER}" >> /etc/hosts

  echo "If you must install as non-root - comment out the docker install below - run it separately, run the user mod, logout/login and continue this script"
  curl https://releases.rancher.com/install-docker/$DOCKER_VERSION.sh | sh
  sudo usermod -aG docker $USERNAME

  echo "install make - required for beijing+ - installed via yum groupinstall Development Tools in RHEL"
  # ubuntu specific
  sudo apt-get install make -y

  sudo docker run -d --restart=unless-stopped -p $PORT:8080 --name rancher_server rancher/server:v$RANCHER_VERSION
  sudo curl -LO https://storage.googleapis.com/kubernetes-release/release/v$KUBECTL_VERSION/bin/linux/amd64/kubectl
  sudo chmod +x ./kubectl
  sudo mv ./kubectl /usr/local/bin/kubectl
  sudo mkdir ~/.kube
  wget http://storage.googleapis.com/kubernetes-helm/helm-v${HELM_VERSION}-linux-amd64.tar.gz
  sudo tar -zxvf helm-v${HELM_VERSION}-linux-amd64.tar.gz
  sudo mv linux-amd64/helm /usr/local/bin/helm

  # create kubernetes environment on rancher using cli
  RANCHER_CLI_VER=0.6.7
  KUBE_ENV_NAME=$ENVIRON
  wget https://releases.rancher.com/cli/v${RANCHER_CLI_VER}/rancher-linux-amd64-v${RANCHER_CLI_VER}.tar.gz
  sudo tar -zxvf rancher-linux-amd64-v${RANCHER_CLI_VER}.tar.gz
  sudo cp rancher-v${RANCHER_CLI_VER}/rancher .
  sudo chmod +x ./rancher

  echo "install jq for json parsing"
  apt install jq -y
  sudo wget https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64 -O jq
  sudo chmod 777 jq
  # not +x or jq will not be runnable from your non-root user
  sudo mv jq /usr/local/bin
  echo "wait for rancher server container to finish - 3 min"
  echo "if you are planning on running a co-located host to bring up more than 110 pods on a single vm - you have 3 min to add --max-pods=900 in additional kublet flags - in the k8s template"
  sleep 60
  echo "2 more min"
  sleep 60
  echo "1 min left"
  sleep 60

  echo "get public and private tokens back to the rancher server so we can register the client later"
  API_RESPONSE=`curl -s 'http://127.0.0.1:8880/v2-beta/apikey' -d '{"type":"apikey","accountId":"1a1","name":"autoinstall","description":"autoinstall","created":null,"kind":null,"removeTime":null,"removed":null,"uuid":null}'`
  # Extract and store token
  echo "API_RESPONSE: $API_RESPONSE"
  KEY_PUBLIC=`echo $API_RESPONSE | jq -r .publicValue`
  KEY_SECRET=`echo $API_RESPONSE | jq -r .secretValue`
  echo "publicValue: $KEY_PUBLIC secretValue: $KEY_SECRET"

  export RANCHER_URL=http://${SERVER}:$PORT
  export RANCHER_ACCESS_KEY=$KEY_PUBLIC
  export RANCHER_SECRET_KEY=$KEY_SECRET
  ./rancher env ls
  echo "wait 60 sec for rancher environments to settle before we create the onap kubernetes one"
  sleep 60

  echo "Creating kubernetes environment named ${KUBE_ENV_NAME}"
  ./rancher env create -t kubernetes $KUBE_ENV_NAME > kube_env_id.json
  PROJECT_ID=$(<kube_env_id.json)
  echo "env id: $PROJECT_ID"
  export RANCHER_HOST_URL=http://${SERVER}:$PORT/v1/projects/$PROJECT_ID
  echo "you should see an additional kubernetes environment usually with id 1a7"
  ./rancher env ls
  # optionally disable cattle env

  # add host registration url
  # https://github.com/rancher/rancher/issues/2599
  # wait for REGISTERING to ACTIVE
  echo "sleep 90 to wait for REG to ACTIVE"
  ./rancher env ls
  sleep 30
  echo "check on environments again before registering the URL response"
  ./rancher env ls
  sleep 30
  ./rancher env ls
  echo "60 more sec"
  sleep 60

  REG_URL_RESPONSE=`curl -X POST -u $KEY_PUBLIC:$KEY_SECRET -H 'Accept: application/json' -H 'ContentType: application/json' -d '{"name":"$SERVER"}' "http://$SERVER:8880/v1/projects/$PROJECT_ID/registrationtokens"`
  echo "REG_URL_RESPONSE: $REG_URL_RESPONSE"
  echo "wait for server to finish url configuration - 5 min"
  sleep 240
  echo "60 more sec"
  sleep 60
  # see registrationUrl in
  REGISTRATION_TOKENS=`curl http://127.0.0.1:$PORT/v2-beta/registrationtokens`
  echo "REGISTRATION_TOKENS: $REGISTRATION_TOKENS"
  REGISTRATION_URL=`echo $REGISTRATION_TOKENS | jq -r .data[0].registrationUrl`
  REGISTRATION_DOCKER=`echo $REGISTRATION_TOKENS | jq -r .data[0].image`
  REGISTRATION_TOKEN=`echo $REGISTRATION_TOKENS | jq -r .data[0].token`
  echo "Registering host for image: $REGISTRATION_DOCKER url: $REGISTRATION_URL registrationToken: $REGISTRATION_TOKEN"
  HOST_REG_COMMAND=`echo $REGISTRATION_TOKENS | jq -r .data[0].command`
  echo "Running agent docker..."
  if [[ "$COMPUTEADDRESS" != false ]]; then
      echo "sudo docker run --rm --privileged -v /var/run/docker.sock:/var/run/docker.sock -v /var/lib/rancher:/var/lib/rancher $REGISTRATION_DOCKER $RANCHER_URL/v1/scripts/$REGISTRATION_TOKEN"
      sudo docker run --rm --privileged -v /var/run/docker.sock:/var/run/docker.sock -v /var/lib/rancher:/var/lib/rancher $REGISTRATION_DOCKER $RANCHER_URL/v1/scripts/$REGISTRATION_TOKEN
  else
      echo "sudo docker run -e CATTLE_AGENT_IP=\"$ADDRESS\" --rm --privileged -v /var/run/docker.sock:/var/run/docker.sock -v /var/lib/rancher:/var/lib/rancher rancher/agent:v$AGENT_VERSION http://$SERVER:$PORT/v1/scripts/$TOKEN"
      sudo docker run -e CATTLE_AGENT_IP="$ADDRESS" --rm --privileged -v /var/run/docker.sock:/var/run/docker.sock -v /var/lib/rancher:/var/lib/rancher rancher/agent:v$AGENT_VERSION http://$SERVER:$PORT/v1/scripts/$REGISTRATION_TOKEN
  fi
  echo "waiting 8 min for host registration to finish"
  sleep 420
  echo "1 more min"
  sleep 60
  
  # base64 encode the kubectl token from the auth pair
  # generate this after the host is registered
  KUBECTL_TOKEN=$(echo -n 'Basic '$(echo -n "$RANCHER_ACCESS_KEY:$RANCHER_SECRET_KEY" | base64 -w 0) | base64 -w 0)
  echo "KUBECTL_TOKEN base64 encoded: ${KUBECTL_TOKEN}"
  # add kubectl config - NOTE: the following spacing has to be "exact" or kubectl will not connect - with a localhost:8080 error
  cat > ~/.kube/config <<EOF
apiVersion: v1
kind: Config
clusters:
- cluster:
    api-version: v1
    insecure-skip-tls-verify: true
    server: "https://$SERVER:$PORT/r/projects/$PROJECT_ID/kubernetes:6443"
  name: "${ENVIRON}"
contexts:
- context:
    cluster: "${ENVIRON}"
    user: "${ENVIRON}"
  name: "${ENVIRON}"
current-context: "${ENVIRON}"
users:
- name: "${ENVIRON}"
  user:
    token: "$KUBECTL_TOKEN"

EOF

  
  echo "Verify all pods up on the kubernetes system - will return localhost:8080 until a host is added"
  echo "kubectl get pods --all-namespaces"
  kubectl get pods --all-namespaces
  echo "upgrade server side of helm in kubernetes"
  if [ "$USERNAME" == "root" ]; then
    helm version
  else
    sudo helm version
  fi
  echo "sleep 90"
  sleep 90
  if [ "$USERNAME" == "root" ]; then
    helm init --upgrade
  else
    sudo helm init --upgrade
  fi
  echo "sleep 90"
  sleep 90
  echo "verify both versions are the same below"
  if [ "$USERNAME" == "root" ]; then
    helm version
  else
    sudo helm version
  fi
  echo "start helm server"
  if [ "$USERNAME" == "root" ]; then
    helm serve &
  else
    sudo helm serve &
  fi
  echo "sleep 30"
  sleep 30
  echo "add local helm repo"
  if [ "$USERNAME" == "root" ]; then
    helm repo add local http://127.0.0.1:8879
    helm repo list
  else
    sudo helm repo add local http://127.0.0.1:8879
    sudo helm repo list
  fi
  echo "To enable grafana dashboard - do this after running cd.sh which brings up onap - or you may get a 302xx port conflict"
  echo "kubectl expose -n kube-system deployment monitoring-grafana --type=LoadBalancer --name monitoring-grafana-client"
  echo "to get the nodeport for a specific VM running grafana"
  echo "kubectl get services --all-namespaces | grep graf"
  kubectl get pods --all-namespaces
  echo "finished!"
}

BRANCH=
SERVER=
ENVIRON=
COMPUTEADDRESS=true
ADDRESS=
VALIDATE=false
USERNAME=ubuntu

while getopts ":b:s:e:u:c:a:l:v" PARAM; do
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
    c)
      COMPUTEADDRESS=${OPTARG}
      ;;
    a)
      ADDRESS=${OPTARG}
      ;;
    l)
      USERNAME=${OPTARG}
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

if [[ -z $BRANCH ]]; then
  usage
  exit 1
fi

install_onap $BRANCH $SERVER $ENVIRON $COMPUTEADDRESS $ADDRESS $USERNAME $VALIDATE

