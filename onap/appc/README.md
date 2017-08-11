####################################################################################################################################################################
## APPC Logging Configuration
====================================================================================================================================================================
APPC has three docker containers -appc_controller_container,sdnc_dgbuilder_container and sdnc_db_container.
The canonical logging implementation is only done for the container, appc_controller_container.
Canonical logs will be generated at /var/log/onap/appc/
====================================================================================================================================================================
## Instructions for deployment
====================================================================================================================================================================
    1. Log provider file is available at the path, /etc/onap/appc/conf.d/, on the host VM.
  2. Copy the canonical 'org.ops4j.pax.logging.cfg' from 'appc' directory (of gerrit) into the path created in step 1.
  3. Create the canonical path for logs, /var/log/onap/appc, on the host VM.
  4. Provide write permissions for "others" users for directory created in step 3.
  5. Volume-Mount: The appc container requires two host paths mapped as volume in the container
        
          5.1. Log file path:
             Host path -      /var/log/onap/appc/ mapped to
        Container path - /var/log/onap/appc/
      5.2. Log provider file path: 
          Host Path         : /etc/onap/appc/conf.d/org.ops4j.pax.logging.cfg  mapped to
                Container path    : ${karaf_data}/etc/org.ops4j.pax.logging.cfg
                Note: The current value of ${karaf_data} is /opt/opendaylight/distribution-karaf-0.4.2-Beryllium-SR2/data, which may change depending upon the karaf version.
####################################################################################################################################################################
