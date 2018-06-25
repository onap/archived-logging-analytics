mkdir target
# reuse the same war for the service pods
cp ../../logging-demo/target/*.war target
docker build -t oomk8s/logging-demo-service -f DockerFile .
docker images | grep logging-demo-service
docker tag oomk8s/logging-demo-service oomk8s/logging-demo-service:0.0.1
docker login
docker push oomk8s/logging-demo-service:0.0.1
#docker run -d -it --rm -p 8888:8080 oomk8s/logging-demo-service:latest
