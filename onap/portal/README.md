##############################################################################################################################################
## Portal Logging Configuration
=================================================================================================
Portal has two docker containers- portal and mariadb.
The canonical logging implementation is only done for the container, portal.
Portal has two sub-modules portal-be and portal-db each one has its own separate logback.xml file.
Canonical logs will be generated at /var/log/onap/portal/
=================================================================================================
## Instructions for deployment
=================================================================================================
### Portal logging deployment for sub-module portal-be
-------------------------------------------------------------------------------------------------
    1. Create the canonical path for log provider, /opt/apache-tomcat-8.0.37/webapps/ECOMPPORTAL/WEB-INF/classes, in the container.
  2. Copy the canonical logback.xml from 'portal-be' directory (of gerrit) into the path mentioned in step 1.
  3. Create the canonical path for logs, /var/log/onap/portal/portal-be on the host VM.
  4. Provide write permissions for "others" users for directory created in step 3.
  5. Volume-Mount: The portal container requires one host path mapped as volume in the container
        
          5.1. Log file path:
             Host path -      /var/log/onap/portal/portal-be/ mapped to 
        Container path - /var/log/onap/portal/portal-be/      
      5.2. Log provider file path:              
        Note: Log provider path cannot be configured externally as it is bundled inside a WAR.  
---------------------------------------------------------------------------------------------------------------------------------------------
### Portal logging deployment for sub-module portal-db
-------------------------------------------------------------------------------------------------
    1. Create the canonical path for log provider, /opt/apache-tomcat-8.0.37/webapps/ECOMPDBCAPP/WEB-INF/classes, in the container.
  2. Copy the canonical logback.xml from 'portal-db' directory (of gerrit) into the path mentioned in step 1.
  3. Create the canonical path for logs, /var/log/onap/portal/portal-db on the host VM.
  4. Provide write permissions for "others" users for directory created in step 3.
  5. Volume-Mount: The portal container requires one host path mapped as volume in the container
        
          5.1. Log file path:
             Host path -      /var/log/onap/portal/portal-db/ mapped to 
        Container path - /var/log/onap/portal/portal-db/
        This is done so that the log files are also visible in the VM and then to the filebeat.
      5.2. Log provider file path:              
        Note: Log provider path cannot be configured externally as it is bundled inside a WAR.
##############################################################################################################################################
