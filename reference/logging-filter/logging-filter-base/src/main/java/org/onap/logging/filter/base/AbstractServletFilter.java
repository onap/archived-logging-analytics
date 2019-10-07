/*-
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

import java.util.Base64;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import org.slf4j.MDC;

public abstract class AbstractServletFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractServletFilter.class);

    protected String getSecureRequestHeaders(HttpServletRequest httpRequest) {
        StringBuilder sb = new StringBuilder();
        String header;
        for (Enumeration<String> e = httpRequest.getHeaderNames(); e.hasMoreElements();) {
            header = e.nextElement();
            sb.append(header);
            sb.append(":");
            if (header.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                String value = httpRequest.getHeader(header);
                if (value != null) {
                    String basicAuthUsername = getBasicAuthUserName(value);
                    if (basicAuthUsername != null) {
                        MDC.put(Constants.MDC.BASIC_AUTH_USER_NAME, basicAuthUsername);
                    }
                }
                sb.append(Constants.REDACTED);
            } else {
                sb.append(httpRequest.getHeader(header));
            }
            sb.append(";");
        }
        return sb.toString();
    }

    protected String formatResponseHeaders(HttpServletResponse response) {
        StringBuilder sb = new StringBuilder();
        for (String headerName : response.getHeaderNames()) {
            sb.append(headerName);
            sb.append(":");
            sb.append(response.getHeader(headerName));
            sb.append(";");
        }
        return sb.toString();
    }

    protected String getBasicAuthUserName(String encodedAuthorizationValue) {
        try {
            // This will strip the word Basic and single space
            encodedAuthorizationValue = encodedAuthorizationValue.substring(6);
            byte[] decodedBytes = Base64.getDecoder().decode(encodedAuthorizationValue);
            String decodedString = new String(decodedBytes);
            int idx = decodedString.indexOf(':');
            return decodedString.substring(0, idx);
        } catch (IllegalArgumentException e) {
            log.error("could not decode basic auth value " + encodedAuthorizationValue, e);
            return null;
        }
    }
}
