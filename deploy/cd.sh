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
# v20100104
# https://wiki.onap.org/display/DW/ONAP+on+Kubernetes
# source from https://jira.onap.org/browse/OOM-320, 326, 321
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
      local MAX_WAIT_PERIODS=80 # 20 MIN
      local COUNTER=1
      local PENDING_PODS=0
      local TARGET_POD_PREFIX=$1
      local PENDING=0
      while [  $PENDING -lt $RUNNING_PODS_LIMIT  ]; do
        PENDING=$(kubectl get pods --all-namespaces | grep $TARGET_POD_PREFIX | grep -E '1/1|2/2' | wc -l)
        PENDING_PODS=$PENDING
        sleep 15
        LIST_PENDING=$(kubectl get pods --all-namespaces -o wide | grep $TARGET_POD_PREFIX | grep -E '1/1|2/2' )
        echo "${PENDING} running < ${RUNNING_PODS_LIMIT} at the ${COUNTER}th 15 sec interval for $TARGET_POD_PREFIX"
        COUNTER=$((COUNTER + 1 ))
        MAX_WAIT_PERIODS=$((MAX_WAIT_PERIODS - 1))
        if [ "$MAX_WAIT_PERIODS" -eq 0 ]; then
          FAILED_PODS_LIMIT=800
        fi
     done
     dt="$(date +"%T")"
     echo "$dt: ${PENDING} pods are up(1/1|2/2) for $TARGET_POD_PREFIX"
}

deploy_onap() {
  
  echo "$(date)"
  echo "running with: -b $BRANCH -e $ENVIRON -p $DOCKER_PREPULL -n $NEXUS3_AND_PORT -f $FULL_MANAGED_DEPLOY -s $SPLIT_DEPLOY_DELAY -c $CLONE_NEW_OOM -d $DELETE_PREV_OOM -w $APPLY_WORKAROUNDS -r $REMOVE_OOM_AT_END"
  echo "provide onap-parameters.yaml(amsterdam) or dev0.yaml+dev1.yaml (master) and aai-cloud-region-put.json"
  echo "provide a dev0.yaml and dev1.yaml override (0=platform, 1=rest of pods) - copy from https://git.onap.org/oom/tree/kubernetes/onap/resources/environments/dev.yaml"
  # fix virtual memory for onap-log:elasticsearch under Rancher 1.6.11 - OOM-431
  sudo sysctl -w vm.max_map_count=262144
  if [[ "$DELETE_PREV_OOM" != false ]]; then
    echo "remove existing oom"
    # master/beijing only - not amsterdam
    if [ "$BRANCH" == "amsterdam" ]; then
      oom/kubernetes/oneclick/deleteAll.bash -n $ENVIRON
    else
      # run undeploy for completeness of the deploy/undeploy cycle - note that pv/pvcs are not deleted in all cases
      # this will fail as expected on a clean first run of the deployment - the plugin will be installed for run n+1
      sudo helm undeploy $ENVIRON --purge
      # workaround for secondary orchestration in dcae
      kubectl delete namespace $ENVIRON
      echo "sleep for 4 min to allow the delete to finish pod terminations before trying a helm delete"
      sleep 240
      sudo helm delete --purge $ENVIRON
    fi

    # verify
    DELETED=$(kubectl get pods --all-namespaces | grep -E '0/|1/2' | wc -l)
    echo "showing $DELETED undeleted pods"
    echo "verify deletion is finished."
    while [  $(kubectl get pods --all-namespaces | grep -E '0/|1/2' | wc -l) -gt 0 ]; do
      sleep 15
      echo "waiting for deletions to complete"
      # addressing rare occurrence on Terminating instances requiring scripted --force in next merge for LOG-914
    done

    # delete potential hanging clustered pods
    #kubectl delete pod $ENVIRON-aaf-sms-vault-0 -n $ENVIRON --grace-period=0 --force
    # specific to when there is no helm release
    kubectl delete pv --all
    kubectl delete pvc --all
    kubectl delete secrets --all
    kubectl delete clusterrolebinding --all
    # keep jenkins 120 sec timeout happy with echos
    sleep 30
    echo "List of ONAP Modules - look for terminating pods"
    LIST_ALL=$(kubectl get pods --all-namespaces -o wide )
    echo "${LIST_ALL}"

    # for use by continuous deployment only
    echo " deleting /dockerdata-nfs/ all onap-* deployments"
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
   
    #sudo helm install local/onap -n onap --namespace $ENVIRON
    dt="$(date +"%T")"
    echo "$dt: starting ONAP install"
    # run an empty deploy first to get a round a random helm deploy failure on a release upgrade failure (deploy plugin runs as upgrade instead of install)
    echo "deploying empty onap deployment as base 1 of 3"
    sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml --verbose
    # deploy platform pods first - dev0 and dev1 can be the same is required
    echo "deploying base onap pods as base 2 of 3"
    sleep $SPLIT_DEPLOY_DELAY
    if [[ "$FULL_MANAGED_DEPLOY" != true ]]; then
      echo "deploying onap subset based on dev0.yaml - use -f true option to bring up all of onap in sequence"
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --verbose
      echo "sleep ${SPLIT_DEPLOY_DELAY} sec to allow base platform pods to complete - without a grep on 0/1|0/2| non-Complete jobs"
      sleep $SPLIT_DEPLOY_DELAY
      echo "deploying rest of onap pods as base 3 of 3"
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev1.yaml --verbose  
    else
      # these arrays are a WIP - not used yet
      DEPLOY_ORDER_PODS=('robot' 'consul' 'aaf' 'dmaap' 'dcaegen2' 'msb' 'aai' 'esr' 'multicloud' 'oof' 'so' 'sdc' 'sdnc' 'vid' 'policy' 'portal')
      DEPLOY_NUMBER_PODS_DESIRED=(1 4 13 11 d  5 14 2 6 13 10 12 11 2) 
      # account for podd that have varying deploy times or replicaset sizes
      DEPOLY_NUMBER_PODS_PARTIAL=(1 2 11 9 d 5 11 2 6 11 10 12 11 2)
      echo "deploying full onap system in dependency order - in sequence for staged use of hd/ram/network resources"
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod robot- 1 # 1 1/1
      # consul is optional but bring it up before onap pods for registration integrity
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod consul- 2 # 2 1/1 missing 2 of 3 rs
#onap          onap-consul-consul-849447d678-5rvqg                     1/1       Running            0          4h
#onap          onap-consul-consul-server-0                             1/1       Running            0          4h
#onap          onap-consul-consul-server-1                             1/1       Running            0          4h
#onap          onap-consul-consul-server-2                             1/1       Running            0          4h
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod aaf- 11 # 10 1/1 1 2/2 missing 2 of 3 rs
#onap          onap-aaf-aaf-cm-5999cf85df-9h59l        1/1       Running     0          5m
#onap          onap-aaf-aaf-cs-659bcc4b74-8ww5b        1/1       Running     0          5m
#onap          onap-aaf-aaf-fs-67dc4f5fd4-2k796        1/1       Running     0          5m
#onap          onap-aaf-aaf-gui-79765d856-zfz57        1/1       Running     0          5m
#onap          onap-aaf-aaf-hello-5fdc4c766f-fqt2m     1/1       Running     0          5m
#onap          onap-aaf-aaf-locate-654c764588-sll5x    1/1       Running     0          5m
#onap          onap-aaf-aaf-oauth-745f87647d-mzhtc     1/1       Running     0          5m
#onap          onap-aaf-aaf-service-54b96965c8-c2gmm   1/1       Running     0          5m
#onap          onap-aaf-aaf-sms-8647967f-kfwhw         1/1       Running     0          5m
#onap          onap-aaf-aaf-sms-preload-fq6w7          0/1       Completed   0          5m
#onap          onap-aaf-aaf-sms-quorumclient-0         1/1       Running     0          5m
#onap          onap-aaf-aaf-sms-quorumclient-1         1/1       Running     0          4m
#onap          onap-aaf-aaf-sms-quorumclient-2         1/1       Running     0          4m
#onap          onap-aaf-aaf-sms-vault-0                2/2       Running     1          5m
#onap          onap-aaf-aaf-sshsm-distcenter-pllcm     0/1       Completed   0          5m
#onap          onap-aaf-aaf-sshsm-testca-7khbn         0/1       Completed   0          5m

      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod dmaap- 9 # ignore mr-kafka and mr - failing on 3.0.0-ONAP
#onap          onap-dmaap-dbc-pg-0                                            1/1       Running             0          4h
#onap          onap-dmaap-dbc-pg-1                                            1/1       Running             0          4h
#onap          onap-dmaap-dbc-pgpool-c5f8498-k5vd2                            1/1       Running             0          4h
#onap          onap-dmaap-dbc-pgpool-c5f8498-lhswh                            1/1       Running             0          4h
#onap          onap-dmaap-dmaap-bus-controller-557dc8c59c-lvzls               1/1       Running             0          4h
#onap          onap-dmaap-dmaap-dr-db-576f7968b8-wj7tn                        1/1       Running             0          4h
#onap          onap-dmaap-dmaap-dr-node-7647f9d6d8-2fbkk                      1/1       Running             0          4h
#onap          onap-dmaap-dmaap-dr-prov-f4d84869f-tg4x6                       1/1       Running             0          4h
# these 2 problematic
#onap          onap-dmaap-message-router-76f4799d-fvbnq                       0/1       Running             42         4h
#onap          onap-dmaap-message-router-kafka-56c7c546c-m8pgp                1/1       Running             9          4h
#onap          onap-dmaap-message-router-zookeeper-7d69b496bc-cp9lq           1/1       Running             0          4h
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set dcaegen2.enabled=true --set msb.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod dcaegen2- 13
#onap          onap-dcaegen2-dcae-bootstrap-8564488869-vblqw                  1/1       Running            1          3h
#onap          onap-dcaegen2-dcae-cloudify-manager-548c847788-2bls9           1/1       Running            0          3h
#onap          onap-dcaegen2-dcae-db-0                                        1/1       Running            0          3h
#onap          onap-dcaegen2-dcate-db-1                                        1/1       Running            0          3h
#onap          onap-dcaegen2-dcae -healthcheck-ff7f4c649-qdpr4                 1/1       Running            0          3h
#onap          onap-dcaegen2-dcae-pgpool-688d65b66d-mc4gj                     1/1       Running            0          3h
#onap          onap-dcaegen2-dcae-pgpool-688d65b66d-njv6z                     1/1       Running            0          3h
#onap          onap-dcaegen2-dcae-redis-0                                     1/1       Running            0          3h
#onap          onap-dcaegen2-dcae-redis-1                                     1/1       Running            0          3h
#onap          onap-dcaegen2-dcae-redis-2                                     1/1       Running            0          3h
#onap          onap-dcaegen2-dcae-redis-3                                     1/1       Running            0          3h
#onap          onap-dcaegen2-dcae-redis-4                                     1/1       Running            0          3h
#onap          onap-dcaegen2-dcae-redis-5                                     1/1       Running            0          3h
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set msb.enabled=true --set dcaegen2.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod msb- 5
#onap          onap-msb-kube2msb-7c55d474ff-6qq7j                             1/1       Running            2          8h
#onap          onap-msb-msb-consul-67c49849fb-24dz6                           1/1       Running            0          8h
#onap          onap-msb-msb-discovery-56c9b95797-kbvlh                        2/2       Running            0          8h
#onap          onap-msb-msb-eag-58bbdcb9f5-4lj4c                              2/2       Running            0          8h
#onap          onap-msb-msb-iag-954b459f4-824vb                               2/2       Running            0          8h

      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set aai.enabled=true  --set msb.enabled=true --set dcaegen2.enabled=true --set dmaap.enabled=true  --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true  --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod aai- 9 # aai is 2-step - aai pod arrives after 60 min
#onap          onap-aai-aai-b4cb46c6c-4jmdw                                   0/1       Init:0/1           0          2h
#onap          onap-aai-aai-babel-696d964845-qlp4s                            2/2       Running            0          8h
#onap          onap-aai-aai-cassandra-0                                       1/1       Running            0          8h
#onap          onap-aai-aai-champ-5f7f94bb84-khvp8                            1/2       Running            0          8h
#onap          onap-aai-aai-data-router-7f6d895dc4-np26m                      1/2       CrashLoopBackOff   161        8h
#onap          onap-aai-aai-elasticsearch-6d7f96f9b8-5zpt8                    1/1       Running            1          8h
#onap          onap-aai-aai-gizmo-5d9c884b9-qlgr6                             2/2       Running            3          8h
#onap          onap-aai-aai-graphadmin-99648f689-cv757                        2/2       Running            0          8h
#onap          onap-aai-aai-graphadmin-create-db-schema-q42tj                 0/1       Completed          0          8h
#onap          onap-aai-aai-modelloader-8b659f647-rf7q8                       2/2       Running            0          8h
#onap          onap-aai-aai-resources-6dc4766986-6q2rz                        2/2       Running            0          8h
#onap          onap-aai-aai-search-data-7dbb99df5f-442dc                      2/2       Running            0          8h
#onap          onap-aai-aai-sparky-be-86db4b4fdf-xzdxz                        0/2       Init:0/1           0          8h
#onap          onap-aai-aai-spike-7bb658d79b-v72lw                            0/2       Init:0/1           47         8h
#onap          onap-aai-aai-traversal-6ffbcb7dbf-9k9b5                        1/2       CrashLoopBackOff   65         8h
#onap          onap-aai-aai-traversal-update-query-data-6kp7s                 0/1       Init:0/1           49         8h
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dcaegen2.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod esr- 2
#onap          onap-esr-esr-gui-7946c6b68d-4h899                              1/1       Running            0          8h
#onap          onap-esr-esr-server-d96fcc7d7-hc4xv                            2/2       Running            0          8h
      # required for so
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dcaegen2.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod multicloud- 6
#onap          onap-multicloud-multicloud-747c4bb789-vpkk6                    2/2       Running            0          8h
#onap          onap-multicloud-multicloud-azure-5678854877-qw8md              2/2       Running            0          8h
#onap          onap-multicloud-multicloud-ocata-578bb5dbb4-rqq8j              2/2       Running            0          8h
#onap          onap-multicloud-multicloud-pike-6b5f695cdf-dc6cj               2/2       Running            0          8h
#onap          onap-multicloud-multicloud-vio-7bf4544fc-fm2rd                 2/2       Running            0          8h
#onap          onap-multicloud-multicloud-windriver-7569857666-sdfth          2/2       Running            0          8h
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dcaegen2.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      # required for so and holmes
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod oof- 16 # 17 (less one of the zookeeper rs 
#onap          onap-oof-cmso-db-0                                             1/1       Running             0          4h
#onap          onap-oof-music-cassandra-0                                     1/1       Running             0          4h
#onap          onap-oof-music-cassandra-1                                     1/1       Running             0          4h
#onap          onap-oof-music-cassandra-2                                     1/1       Running             0          4h
#onap          onap-oof-music-cassandra-job-config-xbwjt                      0/1       Completed           0          4h
#onap          onap-oof-music-tomcat-c7b8f6488-fmjjt                          1/1       Running             0          4h
#onap          onap-oof-music-tomcat-c7b8f6488-k5tt2                          1/1       Running             0          4h
#onap          onap-oof-music-tomcat-c7b8f6488-zpswt                          1/1       Running             0          4h
#onap          onap-oof-oof-5b8c698f4d-ktqlg                                  1/1       Running             0          4h
#onap          onap-oof-oof-cmso-service-6454cf5994-qtrwx                     1/1       Running             0          4h
#onap          onap-oof-oof-has-api-748978b8d4-vmqm7                          1/1       Running             0          4h
#onap          onap-oof-oof-has-controller-797dc5f77f-t649s                   1/1       Running             0          4h
#onap          onap-oof-oof-has-data-678ccbb6c9-j4gvn                         1/1       Running             0          4h
#onap          onap-oof-oof-has-healthcheck-kl4x4                             0/1       Completed           0          4h
#onap          onap-oof-oof-has-onboard-tjgnt                                 0/1       Completed           0          4h
#onap          onap-oof-oof-has-reservation-765c68fc45-wl7x5                  1/1       Running             0          4h
#onap          onap-oof-oof-has-solver-54fb478dfb-qpsq9                       1/1       Running             0          4h
#onap          onap-oof-zookeeper-0                                           1/1       Running             0          4h
#onap          onap-oof-zookeeper-1                                           1/1       Running             0          4h
#onap          onap-oof-zookeeper-2                                           1/1       Running             0          3h
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dcaegen2.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      wait_for_pod so- 10 
      #sleep $SPLIT_DEPLOY_DELAY
#onap          onap-so-so-75bb4b68bb-nf7cf                                    1/1       Running            0          7h
#onap          onap-so-so-bpmn-infra-56f99599b6-qkvd9                         1/1       Running            0          7h
#onap          onap-so-so-catalog-db-adapter-7994778fd9-mf722                 1/1       Running            0          7h
#onap          onap-so-so-mariadb-795cf844d8-9ppn6                            1/1       Running            0          7h
#onap          onap-so-so-monitoring-b75b95f76-vzzc6                          1/1       Running            0          7h
#onap          onap-so-so-openstack-adapter-78dc84b88f-zm4h4                  1/1       Running            2          7h
#onap          onap-so-so-request-db-adapter-64ff55cb79-b4l8z                 1/1       Running            0          7h
#onap          onap-so-so-sdc-controller-d89d44595-cztp6                      1/1       Running            0          7h
#onap          onap-so-so-sdnc-adapter-8db65dfb8-45pgm                        1/1       Running            0          7h
#onap          onap-so-so-vfc-adapter-6c4d54b6fd-4772m                        1/1       Running            0          7h

      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dcaegen2.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod sdc- 12
#onap          onap-sdc-sdc-be-66cb559689-tz655                               2/2       Running            0          7h
#onap          onap-sdc-sdc-be-config-backend-74pqt                           0/1       Completed          0          7h
#onap          onap-sdc-sdc-cs-759f5d9d79-5f2bx                               1/1       Running            1          7h
#onap          onap-sdc-sdc-cs-config-cassandra-v8gn6                         0/1       Completed          0          7h
#onap          onap-sdc-sdc-dcae-be-5b55b7dc9c-7rqrr                          2/2       Running            0          7h
#onap          onap-sdc-sdc-dcae-be-tools-qwvfr                               0/1       Completed          0          6h
#onap          onap-sdc-sdc-dcae-dt-5cd48f7598-9fjf4                          2/2       Running            0          7h
#onap          onap-sdc-sdc-dcae-fe-6c6f66664b-pjtcc                          2/2       Running            0          7h
#onap          onap-sdc-sdc-dcae-tosca-lab-7576c79d74-st5bx                   2/2       Running            0          7h
#onap          onap-sdc-sdc-es-8d55bd56-6mgnx                                 1/1       Running            0          7h
#onap          onap-sdc-sdc-es-config-elasticsearch-2vcrw                     0/1       Completed          0          7h
#onap          onap-sdc-sdc-fe-68c87b558-xwm9z                                2/2       Running            0          7h
#onap          onap-sdc-sdc-kb-6cb4d7d57d-gvmcc                               1/1       Running            0          7h
#onap          onap-sdc-sdc-onboarding-be-7876b44956-gsbhm                    2/2       Running            0          7h
#onap          onap-sdc-sdc-onboarding-be-cassandra-init-5qplk                0/1       Completed          0          7h
#onap          onap-sdc-sdc-wfd-be-85758b765-f9ccw                            1/1       Running            7          7h
#onap          onap-sdc-sdc-wfd-be-workflow-init-nblqf                        0/1       Completed          0          7h
#onap          onap-sdc-sdc-wfd-fe-7947c855d8-xnzhp                           2/2       Running            0          7h
      
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set sdnc.enabled=true --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dcaegen2.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod sdnc- 11
#onap          onap-sdnc-controller-blueprints-bc66576c5-phknj         1/1       Running            1          44m
#onap          onap-sdnc-controller-blueprints-db-0                    1/1       Running            0          44m
#onap          onap-sdnc-nengdb-0                                      1/1       Running            0          44m
#onap          onap-sdnc-network-name-gen-7f95f5bfc8-ptbl6             1/1       Running            0          44m
#onap          onap-sdnc-sdnc-0                                        2/2       Running            0          44m
#onap          onap-sdnc-sdnc-ansible-server-7d595dd8-zbqcd            1/1       Running            0          44m
#onap          onap-sdnc-sdnc-db-0                                     2/2       Running            0          44m
#onap          onap-sdnc-sdnc-dgbuilder-7c8fcbff77-w4688               1/1       Running            0          44m
#onap          onap-sdnc-sdnc-dmaap-listener-fd8bf55cf-q7jv4           1/1       Running            0          44m
#onap          onap-sdnc-sdnc-portal-55976f4bd-h2kkd                   1/1       Running            0          44m
#onap          onap-sdnc-sdnc-ueb-listener-7bf689f8b9-ftld8            1/1       Running            0          44m
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set vid.enabled=true --set sdnc.enabled=true --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dcaegen2.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod vid- 2
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set policy.enabled=true --set vid.enabled=true --set sdnc.enabled=true --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dcaegen2.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod policy- 8
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set portal.enabled=true --set policy.enabled=true --set vid.enabled=true --set sdnc.enabled=true --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dcaegen2.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod portal- 6
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set log.enabled=true --set portal.enabled=true --set policy.enabled=true --set vid.enabled=true --set sdnc.enabled=true --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dcaegen2.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod log- 3
      # above required fdr vFW
      # vCPE related
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set vfc.enabled=true --set log.enabled=true --set portal.enabled=true --set policy.enabled=true --set vid.enabled=true --set sdnc.enabled=true --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dcaegen2.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod vfc- 18
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set uui.enabled=true --set vfc.enabled=true --set log.enabled=true --set portal.enabled=true --set policy.enabled=true --set vid.enabled=true --set sdnc.enabled=true --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dcaegen2.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod uui- 3
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set vnfsdk.enabled=true --set uui.enabled=true --set vfc.enabled=true --set log.enabled=true --set portal.enabled=true --set policy.enabled=true --set vid.enabled=true --set sdnc.enabled=true --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod vnfsdk- 5
      # closed loop
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set appc.enabled=true --set vnfsdk.enabled=true --set uui.enabled=true --set vfc.enabled=true --set log.enabled=true --set portal.enabled=true --set policy.enabled=true --set vid.enabled=true --set sdnc.enabled=true --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set dcaegen2.enabled=true --set msb.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod appc- 5 # 7 with rs
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set dcaegen2.enabled=true --set clamp.enabled=true --set appc.enabled=true --set vnfsdk.enabled=true --set uui.enabled=true --set vfc.enabled=true --set log.enabled=true --set portal.enabled=true --set policy.enabled=true --set vid.enabled=true --set sdnc.enabled=true --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod clamp- 5
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set cli.enabled=true --set dcaegen2.enabled=true --set clamp.enabled=true --set appc.enabled=true --set vnfsdk.enabled=true --set uui.enabled=true --set vfc.enabled=true --set log.enabled=true --set portal.enabled=true --set policy.enabled=true --set vid.enabled=true --set sdnc.enabled=true --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod cli- 1
      # monitoring
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set pomba.enabled=true --set cli.enabled=true --set dcaegen2.enabled=true --set clamp.enabled=true --set appc.enabled=true --set vnfsdk.enabled=true --set uui.enabled=true --set vfc.enabled=true --set log.enabled=true --set portal.enabled=true --set policy.enabled=true --set vid.enabled=true --set sdnc.enabled=true --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod pomba- 8 # 11
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set vvp.enabled=true --set pomba.enabled=true --set cli.enabled=true --set dcaegen2.enabled=true --set clamp.enabled=true --set appc.enabled=true --set vnfsdk.enabled=true --set uui.enabled=true --set vfc.enabled=true --set log.enabled=true --set portal.enabled=true --set policy.enabled=true --set vid.enabled=true --set sdnc.enabled=true --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAY
      wait_for_pod vvp- 10
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set contrib.enabled=true --set vvp.enabled=true --set pomba.enabled=true --set cli.enabled=true --set dcaegen2.enabled=true --set clamp.enabled=true --set appc.enabled=true --set vnfsdk.enabled=true --set uui.enabled=true --set vfc.enabled=true --set log.enabled=true --set portal.enabled=true --set policy.enabled=true --set vid.enabled=true --set sdnc.enabled=true --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      #sleep $SPLIT_DEPLOY_DELAYi
      wait_for_pod contrib- 3
      sudo helm deploy onap local/onap --namespace $ENVIRON -f onap/resources/environments/disable-allcharts.yaml -f ~/dev0.yaml --set sniro-emulator.enabled=true --set contrib.enabled=true --set vvp.enabled=true --set pomba.enabled=true --set cli.enabled=true --set dcaegen2.enabled=true --set clamp.enabled=true --set appc.enabled=true --set vnfsdk.enabled=true --set uui.enabled=true --set vfc.enabled=true --set log.enabled=true --set portal.enabled=true --set policy.enabled=true --set vid.enabled=true --set sdnc.enabled=true --set sdc.enabled=true --set so.enabled=true --set oof.enabled=true --set multicloud.enabled=true --set esr.enabled=true --set aai.enabled=true --set msb.enabled=true --set dmaap.enabled=true --set aaf.enabled=true --set consul.enabled=true --set robot.enabled=true --verbose
      wait_for_pod sniro- 1
    fi
    cd ../../
  fi

  dt="$(date +"%T")"
  echo "$dt: wait for all pods up for 15-80 min"
  FAILED_PODS_LIMIT=0
  MAX_WAIT_PERIODS=480 # 120 MIN
  COUNTER=0
  PENDING_PODS=0
  while [  $(kubectl get pods --all-namespaces | grep -E '0/|1/2' | wc -l) -gt $FAILED_PODS_LIMIT ]; do
    PENDING=$(kubectl get pods --all-namespaces | grep -E '0/|1/2' | wc -l)
    PENDING_PODS=$PENDING
    sleep 15
    LIST_PENDING=$(kubectl get pods --all-namespaces -o wide | grep -E '0/|1/2' )
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
  PENDING=$(kubectl get pods --all-namespaces | grep -E '0/|1/2')
  PENDING_COUNT=$(kubectl get pods --all-namespaces | grep -E '0/|1/2' | wc -l)
  PENDING_COUNT_AAI=$(kubectl get pods -n $ENVIRON | grep aai- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_AAI" -gt 0 ]; then
    echo "down-aai=${PENDING_COUNT_AAI}"
  fi

  # todo don't stop if aai is down
  PENDING_COUNT_APPC=$(kubectl get pods -n $ENVIRON | grep appc- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_APPC" -gt 0 ]; then
    echo "down-appc=${PENDING_COUNT_APPC}"
  fi
  PENDING_COUNT_MR=$(kubectl get pods -n $ENVIRON | grep message-router- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_MR" -gt 0 ]; then
    echo "down-mr=${PENDING_COUNT_MR}"
  fi
  PENDING_COUNT_SO=$(kubectl get pods -n $ENVIRON | grep so- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_SO" -gt 0 ]; then
    echo "down-so=${PENDING_COUNT_SO}"
  fi
  PENDING_COUNT_POLICY=$(kubectl get pods -n $ENVIRON | grep policy- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_POLICY" -gt 0 ]; then
    echo "down-policy=${PENDING_COUNT_POLICY}"
  fi
  PENDING_COUNT_PORTAL=$(kubectl get pods -n $ENVIRON | grep portal- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_PORTAL" -gt 0 ]; then
    echo "down-portal=${PENDING_COUNT_PORTAL}"
  fi
  PENDING_COUNT_LOG=$(kubectl get pods -n $ENVIRON | grep log- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_LOG" -gt 0 ]; then
    echo "down-log=${PENDING_COUNT_LOG}"
  fi
  PENDING_COUNT_ROBOT=$(kubectl get pods -n $ENVIRON | grep robot- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_ROBOT" -gt 0 ]; then
    echo "down-robot=${PENDING_COUNT_ROBOT}"
  fi
  PENDING_COUNT_SDC=$(kubectl get pods -n $ENVIRON | grep sdc- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_SDC" -gt 0 ]; then
    echo "down-sdc=${PENDING_COUNT_SDC}"
  fi
  PENDING_COUNT_SDNC=$(kubectl get pods -n $ENVIRON | grep sdnc- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_SDNC" -gt 0 ]; then
    echo "down-sdnc=${PENDING_COUNT_SDNC}"
  fi
  PENDING_COUNT_VID=$(kubectl get pods -n $ENVIRON | grep vid- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_VID" -gt 0 ]; then
    echo "down-vid=${PENDING_COUNT_VID}"
  fi

  PENDING_COUNT_AAF=$(kubectl get pods -n $ENVIRON | grep aaf- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_AAF" -gt 0 ]; then
    echo "down-aaf=${PENDING_COUNT_AAF}"
  fi
  PENDING_COUNT_CONSUL=$(kubectl get pods -n $ENVIRON | grep consul- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_CONSUL" -gt 0 ]; then
    echo "down-consul=${PENDING_COUNT_CONSUL}"
  fi
  PENDING_COUNT_MSB=$(kubectl get pods -n $ENVIRON | grep msb- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_MSB" -gt 0 ]; then
    echo "down-msb=${PENDING_COUNT_MSB}"
  fi
  PENDING_COUNT_DCAE=$(kubectl get pods -n $ENVIRON | grep dcaegen2- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_DCAE" -gt 0 ]; then
    echo "down-dcae=${PENDING_COUNT_DCAE}"
  fi
  PENDING_COUNT_CLI=$(kubectl get pods -n $ENVIRON | grep cli- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_CLI" -gt 0 ]; then
    echo "down-cli=${PENDING_COUNT_CLI}"
  fi
  PENDING_COUNT_MULTICLOUD=$(kubectl get pods -n $ENVIRON | grep multicloud- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_MULTICLOUD" -gt 0 ]; then
    echo "down-multicloud=${PENDING_COUNT_MULTICLOUD}"
  fi
  PENDING_COUNT_CLAMP=$(kubectl get pods -n $ENVIRON | grep clamp- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_CLAMP" -gt 0 ]; then
    echo "down-clamp=${PENDING_COUNT_CLAMP}"
  fi
  PENDING_COUNT_VNFSDK=$(kubectl get pods -n $ENVIRON | grep vnfsdk- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_VNFSDK" -gt 0 ]; then
    echo "down-vnfsdk=${PENDING_COUNT_VNFSDK}"
  fi
  PENDING_COUNT_UUI=$(kubectl get pods -n $ENVIRON | grep uui- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_UUI" -gt 0 ]; then
    echo "down-uui=${PENDING_COUNT_UUI}"
  fi
  PENDING_COUNT_VFC=$(kubectl get pods -n $ENVIRON | grep vfc- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_VFC" -gt 0 ]; then
    echo "down-vfc=${PENDING_COUNT_VFC}"
  fi
  PENDING_COUNT_KUBE2MSB=$(kubectl get pods -n $ENVIRON | grep kube2msb- | grep -E '0/|1/2' | wc -l)
  if [ "$PENDING_COUNT_KUBE2MSB" -gt 0 ]; then
    echo "down-kube2msb=${PENDING_COUNT_KUBE2MSB}"
  fi
  echo "pending containers=${PENDING_COUNT}"
  echo "${PENDING}"

  echo "check filebeat 2/2 count for ELK stack logging consumption"
  FILEBEAT=$(kubectl get pods --all-namespaces -a | grep 2/)
  echo "${FILEBEAT}"
  echo "sleep 5 min - to allow rest frameworks to finish"
  sleep 300
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
