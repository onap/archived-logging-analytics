mkdir target
cp ../../logging-demo/target/*.war target
docker build -t oomk8s/logging-demo-nbi -f DockerFile .
docker images | grep logging-demo-nbi
docker tag oomk8s/logging-demo-nbi oomk8s/logging-demo-nbi:0.0.1
docker login
docker push oomk8s/logging-demo-nbi:0.0.1
#docker run -d -it --rm -p 8888:8080 oomk8s/logging-demo-nbi:latest
