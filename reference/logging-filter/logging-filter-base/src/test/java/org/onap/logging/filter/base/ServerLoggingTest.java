/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;

public class ServerLoggingTest {
    private ServerLogging serverLogging = ServerLogging.getInstance();

    @Test
    public void serverFqdnTest()throws UnknownHostException {
        if (System.getenv("NODENAME") == null) {
            String serverFQDN = InetAddress.getLocalHost().getCanonicalHostName();
            assertEquals("FQDN value doesn't match local host", serverFQDN, serverLogging.getServerFQDN());
        }
        else {
            assertEquals("FQDN value doesn't match environment variable", System.getenv("NODENAME"), serverLogging.getServerFQDN());
        }
    }
    @Test
    public void serverIPTest() throws UnknownHostException {
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        assertEquals("IP address doesn't match local host IP address", ipAddress, serverLogging.getServerAddr());
    }
}