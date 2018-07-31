This project contains the reference configurations for ONAP logging providers and an Elastic Stack pipeline consisting of Filebeat, Logstash, Elasticsearch and Kibana.
The project also contains several reference logging library abstractions for Java and Python.
See the OOM project for automated deployment of the Elastic Stack pipeline and application of provider configuration as part of bringing up ONAP on kubernetes.
There is a reference slf4j library in https://git.onap.org/logging-analytics/tree/reference/logging-slf4j
To deploy the kubernetes undercloud and provision OOM refer to the deploy subfolder in this project.

Running the logdemonode pod:
see the README.md in the reference/logging-kubernetes/logdemonode directory

