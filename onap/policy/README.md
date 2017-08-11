##############################################################################################################################################
## Policy Logging Configuration
=================================================================================================
Policy has 7 docker containers - pap,pdp,pypdp,drools,mariadb,nexus,brmsGateway.
Each one has its own separate logback.xml file.
The canonical logging implementation is done for the containers- pap,pdp,pypdp,drools.
Canonical logs will be generated at /var/log/onap/policy/
=================================================================================================
## Instructions for deployment
=================================================================================================
### Policy logging deployment for PDP
-------------------------------------------------------------------------------------------------
    1. Log provider file is available at the path, /opt/app/policy/servers/pdp/webapps/pdp/WEB-INF/classes, in the container PDP.
  2. Copy the canonical logback.xml from 'xacml-pdp-rest' directory (of gerrit) into the path mentioned in step 1.
  3. Create the canonical path for logs, /var/log/onap/policy/xacml-pdp-rest, on the host VM.
  4. Provide write permissions for "others" users for directory created in step 3.
  5. Volume-Mount:The pdp container requires one host path mapped as volume in the container
        
          5.1. Log file path:
             Host path -      /var/log/onap/policy/xacml-pdp-rest/ mapped to 
        Container path - /var/log/onap/policy/xacml-pdp-rest/      
      5.2. Log provider file path:              
        Note: Log provider path cannot be configured externally as it is bundled inside a WAR.  
------------------------------------------------------------------------------------------------------------------------------------------
### Policy logging deployment for PAP sub-module ep_sdk_app
------------------------------------------------------------------------------------------------------------------------------------------
    1. Log provider file is available at the path, /opt/app/policy/servers/console/webapps/ecomp/WEB-INF/classes/, is in the container PAP.
  2. Copy the canonical logback.xml from 'ep_sdk_app' directory (of gerrit) into the path mentioned in step 1.
  3. Create the canonical path for logs, /var/log/onap/policy/ep_sdk_app, on the host VM.
  4. Provide write permissions for "others" users for directory created in step 3.
  5. Volume-Mount: The pap container requires one host path mapped as volume in the container
        
          5.1. Log file path:
             Host path -      /var/log/onap/policy/ep_sdk_app/ mapped to 
        Container path - /var/log/onap/policy/ep_sdk_app/      
      5.2. Log provider file path:              
        Note: Log provider path cannot be configured externally as it is bundled inside a WAR. 
------------------------------------------------------------------------------------------------------------------------------------------
### Policy logging deployment for PAP sub-module xacml-pap-rest
------------------------------------------------------------------------------------------------------------------------------------------
    1. Log provider file is available at the path, /opt/app/policy/servers/pap/webapps/pap/WEB-INF/classes/, is in the container PAP.
  2. Copy the canonical logback.xml from 'xacml-pap-rest' directory (of gerrit) into the path mentioned in step 1.
  3. Create the canonical path for logs, /var/log/onap/policy/xacml-pap-rest, on the host VM.
  4. Provide write permissions for "others" users for directory created in step 3.
  5. Volume-Mount: The pap container requires one host path mapped as volume in the container
        
          5.1. Log file path:
             Host path -      /var/log/onap/policy/xacml-pap-rest/ mapped to 
        Container path - /var/log/onap/policy/xacml-pap-rest/      
      5.2. Log provider file path:              
        Note: Log provider path cannot be configured externally as it is bundled inside a WAR. 
------------------------------------------------------------------------------------------------------------------------------------------
### Policy logging deployment for drools
------------------------------------------------------------------------------------------------------------------------------------------
    1. Create the canonical path for log provider, /etc/onap/policy/conf.d/drools/, on the host VM.
    2. Copy the canonical logback.xml from 'drools' directory (of gerrit) into the path created in step 1.
    3. Give the read access to 'others' for the file, /etc/onap/policy/conf.d/drools/logback.xml. This will give access to the 'policy' user used by the policy container to read this file.
  4. Create the canonical path for logs, /var/log/onap/policy/drools, on the host VM.
  5. Provide write permissions for "others" users for directory created in step 4.
  6. Volume-Mount:The drools container requires two host paths mapped as volume in the container
        
          6.1. Log file path:
             Host path -      /var/log/onap/policy/drools/ mapped to 
        Container path - /var/log/onap/policy/drools/      
      6.2. Log provider file path:    
          Host path -      /etc/onap/policy/conf.d/drools/logback.xml mapped to 
        Container path - /opt/app/policy/config/logback.xml
------------------------------------------------------------------------------------------------------------------------------------------
### Policy logging deployment for pypdpserver
------------------------------------------------------------------------------------------------------------------------------------------
    1. Log provider file is available at the path, /opt/app/policy/servers/pypdp/webapps/PyPDPServer/WEB-INF/classes/, is in the container pypdpserver on the host VM.
  2. Copy the canonical logback.xml from 'pypdpserver' directory (of gerrit) into the path mentioned in step 1.
  3. Create the canonical path for logs, /var/log/onap/policy/pypdpserver, on the host VM.
  4. Provide write permissions for "others" users for directory created in step 3.
  5. Volume-Mount:The pypdpserver container requires one host path mapped as volume in the container
        
          5.1. Log file path:
             Host path -      /var/log/onap/policy/pypdpserver/ mapped to 
        Container path - /var/log/onap/policy/pypdpserver/
      5.2. Log provider file path:    
           Note: Log provider path cannot be configured externally as it is bundled inside a WAR.
##############################################################################################################################################
