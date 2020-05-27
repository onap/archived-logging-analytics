/**
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.logging.filter.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerLogging {

    private static final String NODENAME_ENV_VAR = "NODENAME";
    private static Logger logger = LoggerFactory.getLogger(ServerLogging.class);
    private String serverFQDN = null;
    private InetAddress serverAddr = null;

    private ServerLogging() {
        initializeFQDNandIP();
    }

    // Inner class to provide instance of class
    private static class ServerLoggingSingleton {
        private static final ServerLogging INSTANCE = new ServerLogging();
    }

    public static ServerLogging getInstance() {
        return ServerLoggingSingleton.INSTANCE;
    }

    private void initializeFQDNandIP() {
        serverFQDN = System.getenv(NODENAME_ENV_VAR);
        serverAddr = null;
        try {
            serverAddr = InetAddress.getLocalHost();
            if (serverFQDN == null) {
                serverFQDN = serverAddr.getCanonicalHostName();
            }
        } catch (UnknownHostException e) {
            logger.warn("Cannot Resolve Host Name");
        }
    }

    public String getServerFQDN() {
        if (serverFQDN != null)
            return serverFQDN;
        return "";
    }

    public String getServerAddr() {
        if (serverAddr != null) {
            return serverAddr.getHostAddress();
        }
        return "";
    }
}
