.. This work is licensed under a Creative Commons Attribution 4.0 International License.

Release Notes
=============

Version: 1.0 (Amsterdam)
------------------------

:Release Date: 2017-11-XX

**New Features**

This release adds Elastic Stack analytics deployment to OOM, aligns logging provider configurations, and fixes issues with the propagation of transaction IDs and other contextual information.

    - `LOG-1 <https://jira.onap.org/browse/LOG-1>`_ Transaction ID propagation.
    - `LOG-2 <https://jira.onap.org/browse/LOG-2>`_ Standardized logging provider configuration.
    - `LOG-3 <https://jira.onap.org/browse/LOG-3>`_ Elastic Stack reference analytics pipeline.
    - `LOG-4 <https://jira.onap.org/browse/LOG-4>`_ Transaction ID conventions.

**Bug Fixes**

    - `LOG-64 <https://jira.onap.org/browse/LOG-64>`_ Logger field has a length restriction of 36 which needs a fix.
    - `LOG-74 <https://jira.onap.org/browse/LOG-74>`_ Extract componentName from the source path of log files.

**Known Issues**

    - `LOG-43 <https://jira.onap.org/browse/LOG-43>`_
      Unable to find logback xml for DMaaP component.
      Logging file for DMaaP is available in this jar "eelf-core-0.0.1.jar".

    - `LOG-65 <https://jira.onap.org/browse/LOG-65>`_
      SO Logging Provider Config File need correction in Timestamp MDC.
      Logging provider configuration file for SO i.e. logback files requires correction in Timestamp MDC for correct MDC generation in log.
      The current pattern prints Timestamp as 2017-09-25 05:30:07,832. Expected  pattern is - 2017-09-25T05:30:07.832Z.

    - `LOG-80 <https://jira.onap.org/browse/LOG-80>`_ Kibana does not seem to show all the logs from application pods.
      The content of the log directories (/var/log/onap/mso) are not 100% reflected in Kibana.

    - `LOG-88 <https://jira.onap.org/browse/LOG-88>`_
      SO log format error during Health Check - blocking tracking jira for SO-246.

**Security Issues**
      None.

**Upgrade Notes**
      None

**Deprecation Notes**
      None

**Other**
      None

===========

End of Release Notes
