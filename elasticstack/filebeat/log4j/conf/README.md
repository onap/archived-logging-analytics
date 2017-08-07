#Filebeat canonical configuration
----------------------------------
This filebeat configuration is for ONAP components which uses log4j 1.2.X. The components that uses log4j are -
- APPC
- SDNC
- DCAE

#File(s) provided for filebeat deployment
-----------------------------------------
1. filebeat.yml

#Instructions for Deployment
----------------------------
1. Create path /etc/onap/filebeat/conf.d/log4j on the host on which the filebeat has to be installed.
2. The provided filebeat.yml is parameterized and has tokens instead of values for certain deployment specific parameters (like host). These tokens have a syntax as '$[a-zA-Z_]+'. 
3. The tokens are listed in another file called filebeat.properties. These properties are also provided with commented description about them in the file itself. These tokens have to be replaced with the appropriate values as per the deployment environment setup before deployment. 
4. Deployment script should replace the tokens in the filebeat.yml with the values specified in filebeat.properties file.
5. Now, save the final filebeat.yml at location created in step 1.
6. Create path /var/log/onap on the host.
7. Create path /usr/share/onap/filebeat/data on the host. Provide it with write permissions for "other" users.
8. Following is the list of specifications for filebeat container creation -

   Image        - 'docker.elastic.co/beats/filebeat:5.5.0' available in the Elastic Docker Registry.

   Volume mount - The filebeat container must have two host paths mapped as volume in the container
                  1. Host path -      /etc/onap/filebeat/conf.d/log4j/filebeat.yml   mapped to
                     Container path - /usr/share/filebeat/filebeat.yml

                  2. Host path -      /var/log/onap      mapped to
                     Container path - /var/log/onap

                  3. Host path -      /usr/share/onap/filebeat/data      mapped to
                     Container path - /usr/share/filebeat/data

9. The third volume mapping is specific to the particular filebeat on that host. This path i.e. (/usr/share/onap/filebeat/data/) should not be shared and its contents should be unique to that host.