#Kibana canonical configuration
-------------------------------
This kibana configuration is the deployment settings for kibana environment.

#Files provided for kibana deployment
---------------------------------------
# kibana.yml        
# kibana.properties

#Instructions for deployment
----------------------------
1. Create canonical path /etc/onap/kibana/conf.d on the host on which the kibana has to be installed.
1. The kibana.yml is parameterized and has tokens instead of values for certain deployment specific parameters. These tokens has a syntax as '$[a-zA-Z_]+'. 
3. These tokens are listed in another file kibana.properties. These properties are also provided with commented description about them in the file itself. These tokens have to be replaced with the appropriate values as per the deployment environment before deployment.
4. Now, save the final kibana.yml at location created in step 1.
5. Following is the list of specifications for kibana container creation-

   Image          - 'docker.elastic.co/kibana/kibana:5.5.0' available in the Elastic Docker Registry.

   Port mapping   - Default port published by kibana is 5601
                    The container port 5601 should be published to host port 5601
                        
   Volume mapping - The kibana container must have one host paths mapped as volume in the container
                    Host path      - /etc/onap/kibana/conf.d/kibana.yml   mapped to
                    Container path - /usr/share/kibana/config/kibana.yml
					 
6. kibana.properties need not be deployed after the values from it are used.
