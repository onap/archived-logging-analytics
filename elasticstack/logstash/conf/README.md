#Logstash canonical configuration
----------------------------------
This logstash configuration is to sanitize logback/log4j logs from ONAP components. It also includes the deployment settings for logstash environment.

#Files provided for logstash deployment
---------------------------------------
Logstash provides two types of configurations
1. logstash.yml             [Logstash Settings]
2. onap-pipeline.conf       [Pipeline Configurations]

3. onap-pipeline.properties  

#Instructions for deployment
----------------------------
1. Create canonical path /etc/onap/logstash/conf.d/ on the host on which the logstash has to be installed.
2. Save the logstash.yml at location created in step 1.
3. Create canonical path /etc/onap/logstash/conf.d/pipeline/ on the host.
4. The onap-pipeline.conf is parameterized and has tokens instead of values for certain deployment specific parameteres (like port, elastic host etc.). These tokens has a syntax as '$[a-zA-Z_]+'.
5. These tokens are listed in another file onap-pipeline.properties. These properties are also provided with commented description about them in the file itself. These tokens have to be replaced with the appropriate values as per the deployment environment before deployment.
6. Now, save the final onap-pipeline.conf at location created in step 3.
7. Following is the list of specifications for logstash container creation-

   Image        - 'docker.elastic.co/logstash/logstash:5.4.3' available in the Elastic Docker Registry.
   
   Port mapping - The onap-pipeline.conf specifies the port on which logstash listens for events from filebeats. It is defined as a parameter 'port' in the beats section of input configuration. The container should publish the same port with the host port which is configured in the file.
   
                  Example - If the logstash listens on port 5044 specified in onap-pipeline.conf as -
                   input {
                      beats {
						port => 5044
				  	  }
				    }
			       Then the container port 5044 should be published to host port 5044.
			  
   
   Volume mount - The logstash container must have two host directories mapped as volume in the container
                  1. Host path      - /etc/onap/logstash/conf.d/logstash.yml   mapped to
				     Container path - /usr/share/logstash/config/logstash.yml
					 
			      2. Host path      - /etc/onap/logstash/conf.d/pipeline/      mapped to
				     Container path - /usr/share/logstash/pipeline/
8. onap-pipeline.properties need not be deployed after the values from it are used.