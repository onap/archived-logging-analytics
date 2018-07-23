This project contains reference configurations for ONAP logging providers and an Elastic Stack pipeline consisting of Filebeat, Logstash, Elastic Search and Kibana Discover.
The project also contains several reference logging library abstractions.
See the OOM project for automated deployment of the Elastic Stack pipeline and application of provider configuration as part of bringing up ONAP on kubernetes.
To deploy the kubernetes undercloud and provision OOM refer to the deploy subfolder in this project.

Running the logdemonode pod:
- oom and logging-analytics cloned
sudo git clone https://gerrit.onap.org/r/oom
sudo git clone https://gerrit.onap.org/r/logging-analytics

install onap log:
cd oom/kubernetes
/oom/kubernetes$ sudo helm delete --purge onap
/oom/kubernetes$ sudo make all
/oom/kubernetes$ sudo helm install local/onap -n onap --namespace onap -f onap/resources/environments/disable-allcharts.yaml --set log.enabled=false
/oom/kubernetes$ sudo helm upgrade -i onap local/onap --namespace onap -f onap/resources/environments/disable-allcharts.yaml --set log.enabled=true
 
install logdemo:
cd logging-analytics/reference/logging-kubernetes
/logging-analytics/reference/logging-kubernetes$ sudo helm delete --purge logdemonode
/logging-analytics/reference/logging-kubernetes$ sudo make all
/logging-analytics/reference/logging-kubernetes$ sudo helm install local/logdemonode -n logdemonode --namespace onap --set logdemonode.enabled=true
 
rebuild after code change:
/logging-analytics/reference/logging-kubernetes$ sudo helm upgrade -i logdemonode local/logdemonode --namespace onap --set logdemonode.enabled=false
