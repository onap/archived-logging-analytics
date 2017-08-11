##############################################################################################################################################
## SDC logging configuration
=================================================================================================
SDC has two docker containers - sdc-BE and sdc-FE. 
Each one has its own separate logback.xml file.
Canonical logs will be generated at /var/log/onap/sdc/
=================================================================================================
## Instructions for deployment
=================================================================================================
### SDC logging deployment for sdc-FE
-------------------------------------------------------------------------------------------------
  1. Create the canonical path for log provider, /etc/onap/sdc/conf.d/fe, on the host VM.
  2. Copy the logback.xml from 'fe' directory (of gerrit) into the path created in step 1.
  3. Create the canonical path for logs, /var/log/onap/sdc/sdc-fe, on the host VM.
  4. Provide write permissions for "others" user for directory created in step 3.
  5. Volume-Mount: The sdc-FE container requires two host paths mapped as volume in the container
          5.1. Log Provider file path:
              Host path -      /etc/onap/sdc/conf.d/fe/logback.xml mapped to 
        Container path - ${JETTY_BASE}/config/catalog-fe/logback.xml
      5.2. Log file path:
             Host path -      /var/log/onap/sdc/sdc-fe/ mapped to 
        Container path - /var/log/onap/sdc/sdc-fe/
-------------------------------------------------------------------------------------------------
### SDC logging deployment for sdc-BE
-------------------------------------------------------------------------------------------------
  1. Create the canonical path for log provider, /etc/onap/sdc/conf.d/be, on the host VM.
  2. Copy the logback.xml from 'be' directory (of gerrit) into the canonical path created in step 1.
  3. Create the canonical path for logs, /var/log/onap/sdc/sdc-be, on the host VM.
  4. Provide write permissions for "others" user for directory created in step 3.
  5. Volume-Mount: The sdc-BE container requires two host paths mapped as volume in the container
       5.1. Log Provider file path:
       Host path -      /etc/onap/sdc/conf.d/be/logback.xml mapped to
       Container path - ${JETTY_BASE}/config/catalog-be/logback.xml
     5.2. Log file path:
          Host path -      /var/log/onap/sdc/sdc-be/ mapped to
       Container path - /var/log/onap/sdc/sdc-be/
-------------------------------------------------------------------------------------------------
Note - ${JETTY_BASE} in the test environment was set to /var/lib/jetty/
##############################################################################################################################################
