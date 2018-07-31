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
 
results:
kubectl get pods --all-namespaces
onap          logdemonode-logdemonode-5c8bffb468-rx2br   2/2       Running   0          1m
onap          onap-log-elasticsearch-7557486bc4-9h7gf    1/1       Running   0          40m
onap          onap-log-kibana-fc88b6b79-rkpzx            1/1       Running   0          40m
onap          onap-log-logstash-fpzc5                    1/1       Running   0          40m
 
kubectl get services --all-namespaces
onap          log-es                 NodePort    10.43.17.89     <none>        9200:30254/TCP   39m
onap          log-es-tcp             ClusterIP   10.43.120.133   <none>        9300/TCP         39m
onap          log-kibana             NodePort    10.43.73.68     <none>        5601:30253/TCP   39m
onap          log-ls                 NodePort    10.43.107.55    <none>        5044:30255/TCP   39m
onap          log-ls-http            ClusterIP   10.43.48.177    <none>        9600/TCP         39m
onap          logdemonode            NodePort    10.43.0.35      <none>        8080:30258/TCP   55s

Invoke a rest call that generates logs (ENTRY, in-method, EXIT)
curl http://localhost:30258/logging-demo/rest/health/health
true

check records in elasticsearch

curl http://localhost:30254/_search?q=*
{"took":3,"timed_out":false,"_shards":{"total":21,"successful":21,"failed":0},"hits":{"total":2385953.......


