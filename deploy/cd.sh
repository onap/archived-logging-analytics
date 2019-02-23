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
# v20190223
# https://wiki.onap.org/display/DW/ONAP+on+Kubernetes
# source from https://jira.onap.org/browse/OOM-320, 326, 321, 898, 925, 914
# Michael O'Brien
#

usage() {
  cat <<EOF
Usage: $0 [PARAMs]
example 
./cd.sh -b amsterdam -e onap (will rerun onap in the onap namespace, no new repo, no deletion of existing repo, no sdnc workaround, no onap removal at the end
./cd.sh -b master -e onap -s 500 -c true -d true -w true -r true (run as cd server, new oom, delete prev oom, run workarounds, clean onap at the end of the script
./cd.sh -b 3.0.0-ONAP -e onap -p true -n nexus3.onap.org:10001 -f true -s 600 -c true -d false -w true -r false (standard new server/dev environment - use this as the default)
provide a dev0.yaml/dev1.yaml override set (0=platform, 1=rest of pods) - copy from https://git.onap.org/oom/tree/kubernetes/onap/resources/environments/dev.yaml
 note: the managed deploy where -f is true - deploys all of ONAP - for a subset driven by the enabled flags in the dev yaml - use -f false - as the --set enabled flags override in the prior case
-u                  : Display usage
-b [branch]         : branch = master/beijing or amsterdam (required)
-e [environment]    : use the default (onap)
-p [true|false]     : docker prepull (default false)
-n [nexus3 url:port]: nexus3.onap.org:10001 or proxy - used in prepull
-f [true|false]     : managed deploy - time sequenced - defaults to a full deploy
-s [seconds]        : delay between base and rest of onap dual-deployments based on dev0 and dev1.yaml
-c [true|false]     : FLAG clone new oom repo (default: true)
-d [true|false]     : FLAG delete prev oom - (cd build) (default: false)
-w [true|false]     : FLAG apply workarounds  IE: sdnc (default: true)
-r [true|false]     : FLAG remove oom at end of script - for use by CD only (default: false)
EOF
}

wait_for_pod() {
      local RUNNING_PODS_LIMIT=$2
      local MAX_WAIT_PERIODS=140 # 35 MIN
      local COUNTER=1
      local PENDING_PODS=0
      local TARGET_POD_PREFIX=$1
      local PENDING=0
      while [  $PENDING -lt $RUNNING_PODS_LIMIT  ]; do
        PENDING=$(kubectl get pods --all-namespaces | grep $TARGET_POD_PREFIX | grep -E '1/1|2/2|1/3|2/3' | wc -l)
        PENDING_PODS=$PENDING
        sleep 15
        LIST_PENDING=$(kubectl get pods --all-namespaces -o wide | grep $TARGET_POD_PREFIX | grep -E '1/1|2/2|1/3|2/3' )
        echo "${PENDING} running < ${RUNNING_PODS_LIMIT} at the ${COUNTER}th 15 sec interval for $TARGET_POD_PREFIX"
        COUNTER=$((COUNTER + 1 ))
        MAX_WAIT_PERIODS=$((MAX_WAIT_PERIODS - 1))
        if [ "$MAX_WAIT_PERIODS" -eq 0 ]; then
          PENDING=800
        fi
     done
     dt="$(date +"%T")"
     echo "$dt: ${PENDING} pods are up (1/1|2/2|3/3) for $TARGET_POD_PREFIX at the ${COUNTER}th 15 sec interval"
     echo "deployments: note order: helm list"
     sudo helm list
     kubectl get pods --all-namespaces -o wide
}

deploy_onap() {
  echo "$(date)"
  echo "running with: -b $BRANCH -e $ENVIRON -p $DOCKER_PREPULL -n $NEXUS3_AND_PORT -f $FULL_MANAGED_DEPLOY -s $SPLIT_DEPLOY_DELAY -c $CLONE_NEW_OOM -d $DELETE_PREV_OOM -w $APPLY_WORKAROUNDS -r $REMOVE_OOM_AT_END"
  echo "provide onap-parameters.yaml(amsterdam) or dev0.yaml+dev1.yaml (master) and aai-cloud-region-put.json"
  echo "provide a dev0.yaml and dev1.yaml override (0=platform, 1=rest of pods) - copy from https://git.onap.org/oom/tree/kubernetes/onap/resources/environments/dev.yaml"

  if [[ "$BRANCH" == "beijing" ]]; then
    echo "beijing install deployment no longer supported for a full install because of the configmap 1g limit  - use casablanca+ for helm deploy"
    exit 1
  fi

  # fix virtual memory for onap-log:elasticsearch under Rancher 1.6.11 - OOM-431
  sudo sysctl -w vm.max_map_count=262144
  if [[ "$DELETE_PREV_OOM" != false ]]; then
    echo "remove currently deployed pods"
    sudo helm list
    # master/beijing only - not amsterdam
    if [ "$BRANCH" == "amsterdam" ]; then
      oom/kubernetes/oneclick/deleteAll.bash -n $ENVIRON
    else
      echo "kubectl delete namespace $ENVIRON"
      # workaround for secondary orchestration in dcae
      kubectl delete namespace $ENVIRON
      echo "sleep for 4 min to allow the delete to finish pod terminations before trying a helm delete"
      sleep 240
      sudo helm delete --purge $ENVIRON
    fi

    # verify
    DELETED=$(kubectl get pods --namespace $ENVIRON | grep -E '0/|1/2|1/3|2/3' | wc -l)
    echo "showing $DELETED undeleted pods"
    kubectl get pods --namespace $ENVIRON | grep -E '0/|1/2|1/3|2/3'
    echo "verify deletion is finished."

    # this block: for very infrequent rogue pods that are out-of-band of kubernetes - they dont delete without a force delete
    # max number of cycles exits to --force block next
    local MAX_DELETION_WAIT_PERIODS_BEFORE_RUNNING_FORCE=40 # 10 min
    while [  $(kubectl get pods --namespace $ENVIRON  | grep -E '0/|1/2|1/3|2/3' | wc -l) -gt 0 ]; do
      sleep 15
      echo "waiting for deletions to complete, iterations left: $MAX_DELETION_WAIT_PERIODS_BEFORE_RUNNING_FORCE"
      # addressing rare occurrence on Terminating instances requiring scripted --force in next merge for LOG-914
      MAX_DELETION_WAIT_PERIODS_BEFORE_RUNNING_FORCE=$((MAX_DELETION_WAIT_PERIODS_BEFORE_RUNNING_FORCE - 1))
      if [ "$MAX_DELETION_WAIT_PERIODS_BEFORE_RUNNING_FORCE" -eq 0 ]; then
        #https://wiki.onap.org/display/DW/ONAP+Development#ONAPDevelopment-WorkingwithJSONPath
        #export POD_NAMES=$(kubectl get pods --field-selector=status.phase!=Running --all-namespaces -o jsonpath="{.items[*].metadata.name}")
        export POD_NAMES=$(kubectl get pods --namespace $ENVIRON -o jsonpath="{.items[*].metadata.name}")
        echo "--force delete on pods: $POD_NAMES"
        for pod in $POD_NAMES; do
          echo "running: kubectl delete pods $pod --grace-period=0 --force -n $ENVIRON"
          kubectl delete pods $pod --grace-period=0 --force -n $ENVIRON
        done
      fi
    done

    echo "Pod deletions completed - running helm undeploy then kubectl delete pv,pvc,secrets,cluserrolebindings"
    sudo helm undeploy $ENVIRON --purge   
    # specific to when there is no helm release
    kubectl delete pv --all
    kubectl delete pvc --all
    kubectl delete secrets --all
    kubectl delete clusterrolebinding --all
    # keep jenkins 120 sec timeout happy with echos
    sleep 30
    echo "List of ONAP Modules - look for terminating pods - should be none - only the kubernetes system"
    LIST_ALL=$(kubectl get pods --all-namespaces -o wide )
    echo "${LIST_ALL}"

    # for use by continuous deployment only
    echo " deleting /dockerdata-nfs/ all onap-* deployment directories - why: some pod config jobs will not run on a non-empty nfs subdir"
    sudo chmod -R 777 /dockerdata-nfs/*
    rm -rf /dockerdata-nfs/*
  fi

  # for use by continuous deployment only
  if [[ "$CLONE_NEW_OOM" != false ]]; then
    rm -rf oom
    echo "pull new oom"
    git clone -b $BRANCH http://gerrit.onap.org/r/oom
  fi

  # https://wiki.onap.org/display/DW/OOM+Helm+%28un%29Deploy+plugins
  sudo cp -R ~/oom/kubernetes/helm/plugins/ ~/.helm

  if [ "$BRANCH" == "amsterdam" ]; then
    echo "start config pod"
    # still need to source docker variables
    source oom/kubernetes/oneclick/setenv.bash
    #echo "source setenv override"
    echo "moving onap-parameters.yaml to oom/kubernetes/config"
    cp onap-parameters.yaml oom/kubernetes/config
    cd oom/kubernetes/config
    ./createConfig.sh -n $ENVIRON
    cd ../../../
    echo "verify onap-config is 0/1 not 1/1 - as in completed - an error pod - means you are missing onap-parameters.yaml or values are not set in it."
    while [  $(kubectl get pods -n onap -a | grep config | grep 0/1 | grep Completed | wc -l) -eq 0 ]; do
      sleep 15
      echo "waiting for config pod to complete"
    done
  else
    echo "using dev0|1.yaml in working dir"
  fi

  # usually the prepull takes up to 25-300 min - however hourly builds will finish the docker pulls before the config pod is finished
  if [[ "$DOCKER_PREPULL" != false ]]; then
    echo "pre pull docker images - 40+ min for 75G - use a proxy"
    sudo wget https://git.onap.org/logging-analytics/plain/deploy/docker_prepull.sh
    sudo chmod 777 docker_prepull.sh
    # run only on slave nodes vis cloudformation or heat template
    sudo ./docker_prepull.sh -b $BRANCH -s $NEXUS3_AND_PORT -v true
  fi

  echo "start onap pods"
  if [ "$BRANCH" == "amsterdam" ]; then
    cd oom/kubernetes/oneclick
    ./createAll.bash -n $ENVIRON
    cd ../../../
  else
    cd oom/kubernetes/
    sudo make clean
    sudo make all
    sudo make $ENVIRON
   
    local DISABLE_CHARTS_YAML=onap/resources/environments/disable-allcharts.yaml
    local DEV0_YAML=~/dev0.yaml
    local DEV1_YAML=~/dev1.yaml
    #sudo helm install local/onap -n onap --namespace $ENVIRON
    dt="$(date +"%T")"
    echo "$dt: starting ONAP install"
    # run an empty deploy first to get a round a random helm deploy failure on a release upgrade failure (deploy plugin runs as upgrade instead of install)
    echo "deploying empty onap deployment as base 1 of 3"
    sudo helm deploy onap local/onap --namespace $ENVIRON -f $DISABLE_CHARTS_YAML --verbose
    # deploy platform pods first - dev0 and dev1 can be the same is required
    echo "deploying base onap pods as base 2 of 3 - sleep 30 between"
    sleep 30
    
    if [[ "$FULL_MANAGED_DEPLOY" != true ]]; then
      echo "deploying onap subset based on dev0.yaml - use -f true option to bring up all of onap in sequence"
      sudo helm deploy onap local/onap --namespace $ENVIRON -f $DISABLE_CHARTS_YAML -f $DEV0_YAML --verbose
      echo "sleep ${SPLIT_DEPLOY_DELAY} sec to allow base platform pods to complete - without a grep on 0/1|0/2|0/3 non-Complete jobs"
      sleep $SPLIT_DEPLOY_DELAY
      echo "deploying rest of onap pods as base 3 of 3"
      sudo helm deploy onap local/onap --namespace $ENVIRON -f $DISABLE_CHARTS_YAML -f $DEV1_YAML --verbose  
    else
      # for now master and casablanca have the same pod structure
      if [[ "$BRANCH" == "casablanca" ]] || [[ "$BRANCH" == "3.0.0-ONAP" ]] || [[ "$BRANCH" == "3.0.1-ONAP" ]] || [[ "$BRANCH" == "master" ]]; then 
        if [[ "$BRANCH" == "master" ]]; then
          # node DCAEGEN2 must deploy after consul, msb and dmaap but not any later than deploy 5
          DEPLOY_ORDER_POD_NAME_ARRAY=('consul msb dmaap dcaegen2 aaf robot aai esr multicloud oof so sdc sdnc vid policy portal log vfc uui vnfsdk appc clamp cli pomba contrib sniro-emulator')
          # don't count completed pods
          DEPLOY_NUMBER_PODS_DESIRED_ARRAY=(4 5 11 11 13 1 15 2 6 17 10 12 11 2 8 6 3 18 2 5 5 5 1 11 3 1)
          # account for podd that have varying deploy times or replicaset sizes
          # don't count the 0/1 completed pods - and skip most of the ResultSet instances except 1
          # dcae boostrap is problematic
          DEPLOY_NUMBER_PODS_PARTIAL_ARRAY=(2 5 11 9 11 1 11 2 6 16 10 12 11 2 8 6 3 18 2 5 5 5 1 9 3 1)
        else
          # casablanca branches
          DEPLOY_ORDER_POD_NAME_ARRAY=('consul msb dmaap dcaegen2 aaf robot aai esr multicloud oof so sdc sdnc vid policy portal log vfc uui vnfsdk appc clamp cli pomba vvp contrib sniro-emulator')
          # don't count completed pods
          DEPLOY_NUMBER_PODS_DESIRED_ARRAY=(4 5 11 11 13 1 15 2 6 17 10 12 11 2 8 6 3 18 2 5 5 5 1 11 11 3 1) 
          # account for podd that have varying deploy times or replicaset sizes
          # don't count the 0/1 completed pods - and skip most of the ResultSet instances except 1
          # dcae boostrap is problematic
          DEPLOY_NUMBER_PODS_PARTIAL_ARRAY=(2 5 11 9 11 1 11 2 6 16 10 12 11 2 8 6 3 18 2 5 5 5 1 9 11 3 1)
        fi
        echo "deploying for $BRANCH using profile $DEPLOY_ORDER_POD_NAME_ARRAY"
      else
        echo "branch $BRANCH not supported or unknown - check with LOG-326 or LOG-898"
        exit 1
      fi
      
      echo "deploying full onap system in dependency order - in sequence for staged use of hd/ram/network resources"
      # http://tldp.org/HOWTO/Bash-Prog-Intro-HOWTO-7.html
      # iterate over the multidimensional array and build up a deploy list as we deploy each pod
      local DEPLOY_INDEX=0
      local PODS_PARTIAL=0
      local APPENDABLE_ENABLED_FLAGS=
      for POD_NAME in $DEPLOY_ORDER_POD_NAME_ARRAY; do
        PODS_PARTIAL=${DEPLOY_NUMBER_PODS_PARTIAL_ARRAY[$DEPLOY_INDEX]}
        PODS_DESIRED=${DEPLOY_NUMBER_PODS_DESIRED_ARRAY[$DEPLOY_INDEX]}
        echo "deploying $DEPLOY_INDEX for $POD_NAME - expecting $PODS_PARTIAL of a possible $PODS_DESIRED"
        DEPLOY_INDEX=$((DEPLOY_INDEX + 1 ))
        # append --set pod.enabled=true
        APPENDABLE_ENABLED_FLAGS+=" --set "
        APPENDABLE_ENABLED_FLAGS+=$POD_NAME
        APPENDABLE_ENABLED_FLAGS+=".enabled=true"
        echo $APPENDABLE_ENABLED_FLAGS
        sudo helm deploy onap local/onap --namespace $ENVIRON -f $DISABLE_CHARTS_YAML  -f $DEV0_YAML $APPENDABLE_ENABLED_FLAGS --verbose
        #sleep $SPLIT_DEPLOY_DELAY
        # using name- as a match will bring in some extra pods like so-so-vfc and sdnc-sdnc-portal
        # however using name-name will not work as some pods are named msb-kube2msb or oof-cmso not oof-oof
        # using thenamespace-pod will work like onap-oof - keyed on passed in namespace
        wait_for_pod $ENVIRON-$POD_NAME $PODS_PARTIAL  
     done
    fi
    cd ../../
  fi

  dt="$(date +"%T")"
  echo "$dt: wait for all pods up for 15-80 min"
  FAILED_PODS_LIMIT=0
  MAX_WAIT_PERIODS=10  
  if [[ "$FULL_MANAGED_DEPLOY" != true ]]; then
    MAX_WAIT_PERIODS=400 # 100 MIN
  fi
  COUNTER=0
  PENDING_PODS=0
  while [  $(kubectl get pods --all-namespaces | grep -E '0/|1/2|1/3|2/3' | wc -l) -gt $FAILED_PODS_LIMIT ]; do
    PENDING=$(kubectl get pods --all-namespaces | grep -E '0/|1/2|1/3|2/3' | wc -l)
    PENDING_PODS=$PENDING
    sleep 15
    LIST_PENDING=$(kubectl get pods --all-namespaces -o wide | grep -E '0/|1/2|1/2|2/3' )
    echo "${LIST_PENDING}"
    echo "${PENDING} pending > ${FAILED_PODS_LIMIT} at the ${COUNTER}th 15 sec interval"
    echo ""
    COUNTER=$((COUNTER + 1 ))
    MAX_WAIT_PERIODS=$((MAX_WAIT_PERIODS - 1))
    if [ "$MAX_WAIT_PERIODS" -eq 0 ]; then
      FAILED_PODS_LIMIT=800
    fi
  done

  echo "report on non-running containers"
  PENDING=$(kubectl get pods --all-namespaces | grep -E '0/|1/2|1/3|2/3')
  PENDING_COUNT=$(kubectl get pods --all-namespaces | grep -E '0/|1/2|1/3|2/3' | wc -l)
  PENDING_COUNT_AAI=$(kubectl get pods -n $ENVIRON | grep aai- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_AAI" -gt 0 ]; then
    echo "down-aai=${PENDING_COUNT_AAI}"
  fi

  # todo don't stop if aai is down
  PENDING_COUNT_APPC=$(kubectl get pods -n $ENVIRON | grep appc- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_APPC" -gt 0 ]; then
    echo "down-appc=${PENDING_COUNT_APPC}"
  fi
  PENDING_COUNT_MR=$(kubectl get pods -n $ENVIRON | grep message-router- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_MR" -gt 0 ]; then
    echo "down-mr=${PENDING_COUNT_MR}"
  fi
  PENDING_COUNT_SO=$(kubectl get pods -n $ENVIRON | grep so- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_SO" -gt 0 ]; then
    echo "down-so=${PENDING_COUNT_SO}"
  fi
  PENDING_COUNT_POLICY=$(kubectl get pods -n $ENVIRON | grep policy- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_POLICY" -gt 0 ]; then
    echo "down-policy=${PENDING_COUNT_POLICY}"
  fi
  PENDING_COUNT_PORTAL=$(kubectl get pods -n $ENVIRON | grep portal- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_PORTAL" -gt 0 ]; then
    echo "down-portal=${PENDING_COUNT_PORTAL}"
  fi
  PENDING_COUNT_LOG=$(kubectl get pods -n $ENVIRON | grep log- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_LOG" -gt 0 ]; then
    echo "down-log=${PENDING_COUNT_LOG}"
  fi
  PENDING_COUNT_ROBOT=$(kubectl get pods -n $ENVIRON | grep robot- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_ROBOT" -gt 0 ]; then
    echo "down-robot=${PENDING_COUNT_ROBOT}"
  fi
  PENDING_COUNT_SDC=$(kubectl get pods -n $ENVIRON | grep sdc- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_SDC" -gt 0 ]; then
    echo "down-sdc=${PENDING_COUNT_SDC}"
  fi
  PENDING_COUNT_SDNC=$(kubectl get pods -n $ENVIRON | grep sdnc- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_SDNC" -gt 0 ]; then
    echo "down-sdnc=${PENDING_COUNT_SDNC}"
  fi
  PENDING_COUNT_VID=$(kubectl get pods -n $ENVIRON | grep vid- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_VID" -gt 0 ]; then
    echo "down-vid=${PENDING_COUNT_VID}"
  fi

  PENDING_COUNT_AAF=$(kubectl get pods -n $ENVIRON | grep aaf- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_AAF" -gt 0 ]; then
    echo "down-aaf=${PENDING_COUNT_AAF}"
  fi
  PENDING_COUNT_CONSUL=$(kubectl get pods -n $ENVIRON | grep consul- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_CONSUL" -gt 0 ]; then
    echo "down-consul=${PENDING_COUNT_CONSUL}"
  fi
  PENDING_COUNT_MSB=$(kubectl get pods -n $ENVIRON | grep msb- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_MSB" -gt 0 ]; then
    echo "down-msb=${PENDING_COUNT_MSB}"
  fi
  PENDING_COUNT_DCAE=$(kubectl get pods -n $ENVIRON | grep dcaegen2- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_DCAE" -gt 0 ]; then
    echo "down-dcae=${PENDING_COUNT_DCAE}"
  fi
  PENDING_COUNT_CLI=$(kubectl get pods -n $ENVIRON | grep cli- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_CLI" -gt 0 ]; then
    echo "down-cli=${PENDING_COUNT_CLI}"
  fi
  PENDING_COUNT_MULTICLOUD=$(kubectl get pods -n $ENVIRON | grep multicloud- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_MULTICLOUD" -gt 0 ]; then
    echo "down-multicloud=${PENDING_COUNT_MULTICLOUD}"
  fi
  PENDING_COUNT_CLAMP=$(kubectl get pods -n $ENVIRON | grep clamp- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_CLAMP" -gt 0 ]; then
    echo "down-clamp=${PENDING_COUNT_CLAMP}"
  fi
  PENDING_COUNT_VNFSDK=$(kubectl get pods -n $ENVIRON | grep vnfsdk- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_VNFSDK" -gt 0 ]; then
    echo "down-vnfsdk=${PENDING_COUNT_VNFSDK}"
  fi
  PENDING_COUNT_UUI=$(kubectl get pods -n $ENVIRON | grep uui- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_UUI" -gt 0 ]; then
    echo "down-uui=${PENDING_COUNT_UUI}"
  fi
  PENDING_COUNT_VFC=$(kubectl get pods -n $ENVIRON | grep vfc- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_VFC" -gt 0 ]; then
    echo "down-vfc=${PENDING_COUNT_VFC}"
  fi
  PENDING_COUNT_KUBE2MSB=$(kubectl get pods -n $ENVIRON | grep kube2msb- | grep -E '0/|1/2|1/3|2/3' | wc -l)
  if [ "$PENDING_COUNT_KUBE2MSB" -gt 0 ]; then
    echo "down-kube2msb=${PENDING_COUNT_KUBE2MSB}"
  fi
  echo "pending containers=${PENDING_COUNT}"
  echo "${PENDING}"

  echo "check filebeat 2/2|3/3 count for ELK stack logging consumption"
  FILEBEAT=$(kubectl get pods --all-namespaces -a | grep -E '2/|3/')
  echo "${FILEBEAT}"
  echo "List of ONAP Modules"
  LIST_ALL=$(kubectl get pods --all-namespaces -a -o wide )
  echo "${LIST_ALL}"
  echo "run healthcheck 2 times to warm caches and frameworks so rest endpoints report properly - see OOM-447"

  echo "curl with aai cert to cloud-region PUT"

  curl -X PUT https://127.0.0.1:30233/aai/v11/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/RegionOne --data "@aai-cloud-region-put.json" -H "authorization: Basic TW9kZWxMb2FkZXI6TW9kZWxMb2FkZXI=" -H "X-TransactionId:jimmy-postman" -H "X-FromAppId:AAI" -H "Content-Type:application/json" -H "Accept:application/json" --cacert aaiapisimpledemoopenecomporg_20171003.crt -k

  echo "get the cloud region back"
  curl -X GET https://127.0.0.1:30233/aai/v11/cloud-infrastructure/cloud-regions/ -H "authorization: Basic TW9kZWxMb2FkZXI6TW9kZWxMb2FkZXI=" -H "X-TransactionId:jimmy-postman" -H "X-FromAppId:AAI" -H "Content-Type:application/json" -H "Accept:application/json" --cacert aaiapisimpledemoopenecomporg_20171003.crt -k

  # OOM-484 - robot scripts moved
  cd oom/kubernetes/robot
  echo "run healthcheck prep 1"
  # OOM-722 adds namespace parameter
  if [ "$BRANCH" == "amsterdam" ]; then
    ./ete-k8s.sh health > ~/health1.out
  else
    ./ete-k8s.sh $ENVIRON health > ~/health1.out
  fi
  echo "sleep 5 min"
  sleep 300
  echo "run healthcheck prep 2"
  if [ "$BRANCH" == "amsterdam" ]; then
    ./ete-k8s.sh health > ~/health2.out
  else
    ./ete-k8s.sh $ENVIRON health > ~/health2.out
  fi
  echo "run healthcheck for real - wait a further 5 min"
  sleep 300
  if [ "$BRANCH" == "amsterdam" ]; then
    ./ete-k8s.sh health
  else
    ./ete-k8s.sh $ENVIRON health
  fi
  echo "run partial vFW"
  echo "report results"
  cd ../../../
  
  echo "$(date)"
  #set +a
}

BRANCH=
ENVIRON=onap
FULL_MANAGED_DEPLOY=true
APPLY_WORKAROUNDS=true
DELETE_PREV_OOM=false
REMOVE_OOM_AT_END=false
CLONE_NEW_OOM=true
SPLIT_DEPLOY_DELAY=600
DOCKER_PREPULL=false
NEXUS3_AND_PORT=nexus3.onap.org:10001

while getopts ":u:b:e:p:n:s:f:c:d:w:r" PARAM; do
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
    p)
      DOCKER_PREPULL=${OPTARG}
      ;;
    n)
      NEXUS3_AND_PORT=${OPTARG}
      ;;
    s)
      SPLIT_DEPLOY_DELAY=${OPTARG}
      ;;
    f)
      FULL_MANAGED_DEPLOY=${OPTARG}
      ;;
    c)
      CLONE_NEW_OOM=${OPTARG}
      ;;
    d)
      DELETE_PREV_OOM=${OPTARG}
      ;;
    w)
      APPLY_WORKAROUNDS=${OPTARG}
      ;;
    r)
      REMOVE_OOM_AT_END=${OPTARG}
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

deploy_onap  $BRANCH $ENVIRON $DOCKER_PREPULL $NEXUS3_AND_PORT $SPLIT_DEPLOY_DELAY $FULL_MANAGED_DEPLOY $CLONE_NEW_OOM $DELETE_PREV_OOM $APPLY_WORKAROUNDS $REMOVE_OOM_AT_END

printf "**** Done ****\n"
