####################################################################################################################################################################
## AAI Logging Configuration
====================================================================================================================================================================
AAI has two docker containers- aai-service, hbase-1.2.3 and model-loader-service.
Each container has its own logback.xml
Canonical logs will be generated at /var/log/onap/aai/
====================================================================================================================================================================
## Instructions for deployment
====================================================================================================================================================================
### AAI logging deployment for aai-service
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
	1. Log provider file is available at the path, /etc/onap/aai/conf.d/, on the host VM.
  2. Copy the canonical ‘logback.xml’ from 'aai' directory (of gerrit) into the path created in step 1.
  3. Give the read access to 'others' for the file, /etc/onap/aai/conf.d/logback.xml. 
     This will give access to the 'aaiadmin' user used by the AAI container to read this file.
  4. Create the canonical path for logs, /var/log/onap/aai, on the host VM.
  5. Provide write permissions for "others" users for directory created in step 4.
  6. Volume-Mount: The aai container requires two host paths mapped as volume in the container
        
  		6.1. Log file path:
     		Host path -      /var/log/onap/aai/ mapped to 
        Container path - /var/log/onap/aai/      
      6.2. Log provider file path: 
      	Host Path 		: /etc/onap/aai/conf.d/logback.xml mapped to
				Container path	: /opt/app/aai/bundleconfig/etc/logback.xml    	 	
-------------------------------------------------------------------------------------------------------------------------------------------------------------------
### AAI logging deployment for Model-Loader 
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
	1. Log provider file is available at the path, /opt/jetty/jetty*/webapps/model-loader/WEB-INF/classes/, in the container.
		Note: Inside model-loader docker image. (jetty*  refers to the particular version being used.
		      In release-1.0 environment, the name is '/jetty-distribution-9.3.9.v20160517')
  2. Copy the canonical ‘logback.xml’ from 'aai' directory (of gerrit) into the path created in step 1.
  3. Give the read access to 'others' for the file, /etc/onap/aai/conf.d/logback.xml. 
     This will give access to the 'aaiadmin' user used by the AAI container to read this file.
  4. Create the canonical path for logs, /var/log/onap/aai/aai-ml, on the host VM.
  5. Provide write permissions for "others" users for directory created in step 4.
  6. Volume-Mount: The aai container requires one host path mapped as volume in the container
        
  		6.1. Log file path:
     		Host path -      /var/log/onap/aai/aai-ml mapped to 
        Container path - /var/log/onap/aai/aai-ml      
      6.2. Log provider file path: 
      	Note: Log provider path cannot be configured externally as it is bundled inside a WAR.   	 	
####################################################################################################################################################################
