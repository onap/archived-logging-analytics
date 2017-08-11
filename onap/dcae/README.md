####################################################################################################################################################################
## DCAE Logging Configuration
====================================================================================================================================================================
DCAE has one docker container - dcaestartupvmcontroller_dcae-controller_1.
Canonical logs will be generated at /var/log/onap/dcae/
====================================================================================================================================================================
## Instructions for deployment
====================================================================================================================================================================
    1. Log provider file is available at the path, /etc/onap/dcae/conf.d/, on the host VM.
  2. Copy the canonical 'log4j.properties' from 'dcae' directory (of gerrit) into the path created in step 1.
  3. Create the canonical path for logs, /var/log/onap/dcae, on the host VM.
  4. Provide write permissions for "others" users for directory created in step 3.
  5. Volume-Mount: The dcae container requires two host paths mapped as volume in the container
        
          5.1. Log file path:
             Host path -      /var/log/onap/dcae/ mapped to 
        Container path - /var/log/onap/dcae/      
      5.2. Log provider file path: 
          Host Path         : /etc/onap/dcae/conf.d/log4j.properties mapped to
                Container path    : /opt/app/dcae-controller-platform-server/config/log4j.properties
####################################################################################################################################################################
