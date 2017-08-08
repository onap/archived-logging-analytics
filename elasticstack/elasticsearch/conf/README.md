#Elasticsearch canonical configuration
--------------------------------------
This elasticsearch configuration is the deployment settings for elasticsearch environment.


#Files provided for elasticsearch deployment
--------------------------------------------
1. elasticsearch.yml
2. elasticsearch.properties

#Instructions for deployment
----------------------------
1. Create path /etc/onap/elasticsearch/conf.d on the host on which the elasticsearch has to be installed.
2. The elasticsearch.yml is parameterized and has tokens instead of values for certain deployment specific parameters. These tokens has a syntax as '$[a-zA-Z_]+'. 
3. These tokens are listed in another file elasticsearch.properties. These properties are also provided with commented description about them in the file itself. These tokens have to be replaced with the appropriate values as per the deployment environment before deployment.
4. Now, save the final elasticsearch.yml at location created in step 1.
5. Create path /etc/onap/elasticsearch/data on the host. Provide it with write permissions for 'other' users.
6. Following is the list of specifications for elasticsearch container creation-

   Image        - 'docker.elastic.co/elasticsearch/elasticsearch:5.4.0' available in the Elastic Docker Registry.

   Port mapping -  Elasticsearch requires to publish two ports to host which are specified in the elasticsearch.yml as 'http.port' and 'transport.tcp.port'.
                   Example - If the http.port is set to 9200 and transport.tcp.port to 9300.
                     The container port 9200 should be published to host port 9200
                     The container port 9300 should be published to host port 9300

   Volume mount - The Elasticsearch container must have three host paths mapped as volume in the container
   
					Configuration file path:
                  1. Host path      - /etc/onap/elasticsearch/conf.d/elasticsearch.yml   mapped to
                     Container path - /usr/share/elasticsearch/config/elasticsearch.yml

					Data File path:
                  2. Host path      - /usr/share/onap/elasticsearch/data/ 		mapped to
                     Container path - /usr/share/elasticsearch/data
					
					 The container data file path above is the value of "path.data:" specified in "elasticsearch.yml".
					 
					Log File Path:
				  3. Host path      - /var/log/onap/elasticsearch/		mapped to
                     Container path - /usr/share/elasticsearch/logs
					
					 The container log file path above is the value of "path.logs:" specified in "elasticsearch.yml".
					 
7. The Data File path volume mapping is specific to the particular elasticsearch instance on that host. This path i.e.(/etc/onap/elasticsearch/data) should not be shared and its content should be unique to that host.
8. The elasticsearch.properties need not be deployed ones the values from it are used.