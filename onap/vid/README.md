##############################################################################################################################################
## VID logging configuration
=====================================================================================
VID has two docker containers - vid-server and vid-mariadb. 
The canonical logging implementation is only done for the container, vid-server.
Canonical logs will be generated at /var/log/onap/vid/
=================================================================================================
## Instructions for deployment
=================================================================================================
  1. Log provider file is available at the path, /tomcat/webapps/vid/WEB-INF/classes/, in the container
  2. Copy the canonical logback.xml from 'vid' directory (of gerrit) into the path mentioned in step 1.
  3. Create the canonical path, /var/log/onap/vid/, on the host VM.
  4. Provide write permissions for "others" user for directory created in step 3.
  5. Volume-Mount: The vid-server container requires one host path mapped as volume in the container
       5.1. Log file path:
        Host path -      /var/log/onap/vid/ mapped to 
        Container path - /var/log/onap/vid/        
       5.2. Log Provider file path:     
        Note: Log provider path cannot be configured externally as it is bundled inside a WAR.  
##############################################################################################################################################
