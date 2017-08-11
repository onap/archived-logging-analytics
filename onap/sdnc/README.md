####################################################################################################################################################################
## SDNC Logging Configuration
====================================================================================================================================================================
SDNC has four docker containers -sdnc_dgbuilder_container, sdnc_portal_container, sdnc_controller_container,sdnc_db_container.
The canonical logging implementation is only done for the container, sdnc_controller_container.
Canonical logs will be generated at /var/log/onap/sdnc/
====================================================================================================================================================================
## Instructions for deployment
====================================================================================================================================================================
  1. Create the canonical path for log provider, /etc/onap/sdnc/conf.d/,on the host VM.
  2. Copy the canonical ‘org.ops4j.pax.logging.cfg’ from 'sdnc' directory (of gerrit) into the path created in step 1.
  3. Create the canonical path for logs, /var/log/onap/sdnc, on the host VM.
  4. Provide write permissions for "others" users for directory created in step 3.
  5. Volume-Mount: The sdnc container requires two host paths mapped as volume in the container
        
      5.1. Log file path:
        Host path -      /var/log/onap/sdnc/ mapped to
        Container path - /var/log/onap/sdnc/
      5.2. Log provider file path: 
          Host Path         : /etc/onap/sdnc/conf.d/org.ops4j.pax.logging.cfg mapped to
                Container path    : ${karaf_data}/etc/org.ops4j.pax.logging.cfg
                Note: The current value of ${karaf_data} is /opt/opendaylight/distribution-karaf-0.4.2-Beryllium-SR2/data, which may change depending upon the karaf version.
####################################################################################################################################################################
