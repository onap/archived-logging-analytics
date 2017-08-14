####################################################################################################################################################################
## SO Logging Configuration
====================================================================================================================================================================
SO has 2 docker containers with names - testlab_mso_1 and testlab_mariadb_1.
The canonical logging implementation is only done for the container, testlab_mso_1.
SO has 9 sub-modules and each module has its own logback.xml file.
Canonical logs will be generated at /var/log/onap/so/
====================================================================================================================================================================
## Instructions for deployment
====================================================================================================================================================================
### SO logging deployment for sub-modules
-------------------------------------------------------------------------------------------------------------------------------------------------------------------
    1. Create the canonical path for log provider, /etc/onap/so/conf.d/, on the host VM.
  2. Copy the canonical logback.xmls listed below from 'so' directory (of gerrit) into the path created in step 1.
       2.1. logback.apihandler-infra.xml
             2.2. logback.appc.xml
             2.3. logback.asdc.xml
             2.4. logback.bpmn.xml
             2.5. logback.msorequestsdbadapter.xml
             2.6. logback.network.xml
             2.7. logback.sdnc.xml
             2.8. logback.tenant.xml
             2.9. logback.vnf.xml
  3. Create the canonical path for logs, /var/log/onap/so, on the host VM.
  4. Provide write permissions for "others" users for directory created in step 3.
  5. Volume-Mount: The so container requires two host paths mapped as volume in the container
          5.1. Log file path:
             Host path -      /var/log/onap/so/ mapped to 
        Container path - /var/log/onap/so/      
      5.2. Log provider file path has two mappings: 
          # The below container paths are where the provider files are kept initially
                         /etc/onap/so/conf.d/logback.apihandler-infra.xml           : /var/berks-cookbooks/mso-config/files/default/mso-api-handler-infra-config/logback.apihandler-infra.xml
                         /etc/onap/so/conf.d/logback.network.xml                         : /var/berks-cookbooks/mso-config/files/default/mso-po-adapter-config/logback.network.xml
                         /etc/onap/so/conf.d/logback.tenant.xml                           : /var/berks-cookbooks/mso-config/files/default/mso-po-adapter-config/logback.tenant.xml
                         /etc/onap/so/conf.d/logback.vnf.xml                               : /var/berks-cookbooks/mso-config/files/default/mso-po-adapter-config/logback.vnf.xml
                         /etc/onap/so/conf.d/logback.appc.xml                             : /var/berks-cookbooks/mso-config/files/default/mso-appc-adapter-config/logback.appc.xml
                         /etc/onap/so/conf.d/logback.msorequestsdbadapter.xml     : /var/berks-cookbooks/mso-config/files/default/mso-requests-db-adapter-config/logback.msorequestsdbadapter.xml
                         /etc/onap/so/conf.d/logback.asdc.xml                             : /var/berks-cookbooks/mso-config/files/default/mso-asdc-controller-config/logback.asdc.xml
                         /etc/onap/so/conf.d/logback.sdnc.xml                             : /var/berks-cookbooks/mso-config/files/default/mso-sdnc-adapter-config/logback.sdnc.xml
                         /etc/onap/so/conf.d/logback.bpmn.xml                             : /var/berks-cookbooks/mso-config/files/default/mso-bpmn-config/logback.bpmn.xml

             # The below container paths are where the provider files are copied to after the docker starts
           /etc/onap/so/conf.d/logback.apihandler-infra.xml            : /etc/mso/config.d/logback.apihandler-infra.xml
           /etc/onap/so/conf.d/logback.network.xml                            : /etc/mso/config.d/logback.network.xml
           /etc/onap/so/conf.d/logback.tenant.xml                            : /etc/mso/config.d/logback.tenant.xml
           /etc/onap/so/conf.d/logback.vnf.xml                                  : /etc/mso/config.d/logback.vnf.xml
           /etc/onap/so/conf.d/logback.appc.xml                              : /etc/mso/config.d/logback.appc.xml
           /etc/onap/so/conf.d/logback.msorequestsdbadapter.xml      : /etc/mso/config.d/logback.msorequestsdbadapter.xml
           /etc/onap/so/conf.d/logback.asdc.xml                              : /etc/mso/config.d/logback.asdc.xml
           /etc/onap/so/conf.d/logback.sdnc.xml                              : /etc/mso/config.d/logback.sdnc.xml
           /etc/onap/so/conf.d/logback.bpmn.xml                              : /etc/mso/config.d/logback.bpmn.xml
####################################################################################################################################################################
