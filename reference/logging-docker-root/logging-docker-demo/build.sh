mkdir target
cp ../../logging-demo/target/*.war target
docker build -t obrienlabs/logging-demo-nbi -f DockerFile .
docker images | grep logging-demo-nbi
docker tag obrienlabs/logging-demo-nbi obrienlabs/logging-demo-nbi:0.0.1
docker login
docker push obrienlabs/logging-demo-nbi:0.0.1
#docker run -d -it --rm -p 8888:8080 obrienlabs/logging-demo-nbi:latest
