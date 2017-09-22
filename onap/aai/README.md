####################################################################################################################################################################
## AAI Logging Configuration
====================================================================================================================================================================
AAI has two vm instances 
vm1-aai-inst1 has docker containers- testconfig_aai.searchservice.simpledemo.openecomp.org_1,testconfig_datarouter_1,
testconfig_aai-traversal.api.simpledemo.openecomp.org_1,testconfig_aai-resources.api.simpledemo.openecomp.org_1,testconfig_sparky-be_1,testconfig_model-loader_1,
vm1-aai-inst2 has docker containers- elasticsearch,testconfig_aai.gremlinserver.simpledemo.openecomp.org_1,testconfig_aai.hbase.simpledemo.openecomp.org_1.
Each container has its own logback.xml
Canonical logs will be generated at /var/log/onap/aai/
====================================================================================================================================================================
## Instructions for deployment
====================================================================================================================================================================
### AAI logging deployment for search_search.amdocs.lab_1
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
  1. Create the log provider file path, /etc/onap/aai/conf.d/aai-search, on the host VM.
  2. Copy the canonical ‘logback.xml’ from 'aai' directory (of gerrit) into the path created in step 1.
  3. Give the read access to 'others' for the file, /etc/onap/aai/conf.d/aai-search/logback.xml. 
     This will give access to the 'aaiadmin' user used by the AAI container to read this file.
  4. Create the canonical path for logs, /var/log/onap/aai/aai-search, on the host VM.
  5. Provide write permissions for "others" users for directory created in step 4.
  6. Volume-Mount: The aai container requires two host paths mapped as volume in the container
        
      6.1. Log file path:
     	Host path -      /var/log/onap/aai/aai-search mapped to 
        Container path - /var/log/onap/aai/aai-sdb      
      6.2. Log provider file path: 
      	Host Path 		: /etc/onap/aai/conf.d/aai-search/logback.xml mapped to
	Container path	        : /opt/app/search-data-service/bundleconfig/etc/logback.xml
-------------------------------------------------------------------------------------------------------------------------------------------------------------------
### AAI logging deployment for sparky_sparky.amdocs.lab_1 
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
  1. Create the log provider file path, /etc/onap/aai/conf.d/aai-sparky, on the host VM.
  2. Copy the canonical ‘logback.xml’ from 'aai' directory (of gerrit) into the path created in step 1.
  3. Give the read access to 'others' for the file, /etc/onap/aai/conf.d/aai-search/logback.xml. 
     This will give access to the 'aaiadmin' user used by the AAI container to read this file.
  4. Create the canonical path for logs, /var/log/onap/aai/aai-sparky, on the host VM.
  5. Provide write permissions for "others" users for directory created in step 4.
  6. Volume-Mount: The aai container requires two host paths mapped as volume in the container
        
      6.1. Log file path:
     	Host path -      /var/log/onap/aai/aai-sparky mapped to 
        Container path - /var/log/onap/aai/aai-ui     
      6.2. Log provider file path: 
      	Host Path 		: /etc/onap/aai/conf.d/aai-sparky/logback.xml mapped to
	Container path	        : /opt/app/sparky/bundleconfig/etc/logback.xml
-------------------------------------------------------------------------------------------------------------------------------------------------------------------
### AAI logging deployment for modelloader_model-loader.amdocs.lab_1 
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
  1. Create the log provider file path, /etc/onap/aai/conf.d/aai-model-loader, on the host VM.
  2. Copy the canonical ‘logback.xml’ from 'aai' directory (of gerrit) into the path created in step 1.
  3. Give the read access to 'others' for the file, /etc/onap/aai/conf.d/aai-model-loader/logback.xml. 
     This will give access to the 'aaiadmin' user used by the AAI container to read this file.
  4. Create the canonical path for logs, /var/log/onap/aai/aai-model-loader, on the host VM.
  5. Provide write permissions for "others" users for directory created in step 4.
  6. Volume-Mount: The aai container requires two host paths mapped as volume in the container
        
      6.1. Log file path:
     	Host path -      /var/log/onap/aai/aai-model-loader mapped to 
        Container path - /var/log/onap/aai/aai-ml   
      6.2. Log provider file path: 
      	Host Path 		: /etc/onap/aai/conf.d/aai-model-loader/logback.xml mapped to
	Container path	        : /opt/app/model-loader/bundleconfig/etc/logback.xml
-------------------------------------------------------------------------------------------------------------------------------------------------------------------
### AAI logging deployment for testconfig_aai-traversal.api.simpledemo.openecomp.org_1
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
  1. Create the log provider file path, /etc/onap/aai/conf.d/aai-traversal, on the host VM.
  2. Copy the canonical ‘logback.xml’ from 'aai' directory (of gerrit) into the path created in step 1.
  3. Give the read access to 'others' for the file, /etc/onap/aai/conf.d/aai-traversal/logback.xml. 
     This will give access to the 'aaiadmin' user used by the AAI container to read this file.
  4. Create the canonical path for logs, /var/log/onap/aai/aai-traversal, on the host VM.
  5. Provide write permissions for "others" users for directory created in step 4.
  6. Volume-Mount: The aai container requires two host paths mapped as volume in the container
        
      6.1. Log file path:
     	Host path -      /var/log/onap/aai/aai-traversal mapped to 
        Container path - /var/log/onap/aai/  
      6.2. Log provider file path: 
      	Host Path 		: /etc/onap/aai/conf.d/aai-traversal/logback.xml mapped to
	Container path	        : /opt/app/aai-traversal/bundleconfig/etc/logback.xml
-------------------------------------------------------------------------------------------------------------------------------------------------------------------
### AAI logging deployment for testconfig_aai-resources.api.simpledemo.openecomp.org_1
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
  1. Create the log provider file path, /etc/onap/aai/conf.d/aai-resources, on the host VM.
  2. Copy the canonical ‘logback.xml’ from 'aai' directory (of gerrit) into the path created in step 1.
  3. Give the read access to 'others' for the file, /etc/onap/aai/conf.d/aai-resources/logback.xml. 
     This will give access to the 'aaiadmin' user used by the AAI container to read this file.
  4. Create the canonical path for logs, /var/log/onap/aai/aai-resources, on the host VM.
  5. Provide write permissions for "others" users for directory created in step 4.
  6. Volume-Mount: The aai container requires two host paths mapped as volume in the container
        
      6.1. Log file path:
     	Host path -      /var/log/onap/aai/aai-resources mapped to 
        Container path - /var/log/onap/aai/aai-resources  
      6.2. Log provider file path: 
      	Host Path 		: /etc/onap/aai/conf.d/aai-resources/logback.xml mapped to
	Container path	        : /opt/app/aai-resources/bundleconfig/etc/logback.xml
####################################################################################################################################################################
