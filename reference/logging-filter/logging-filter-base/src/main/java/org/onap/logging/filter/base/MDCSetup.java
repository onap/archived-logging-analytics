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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class MDCSetup {

    protected static Logger logger = LoggerFactory.getLogger(MDCSetup.class);
    private static final String INSTANCE_UUID = UUID.randomUUID().toString();
    protected static final String serverIpAddressOverride = "SERVER_IP_ADDRESS_OVERRIDE";
    protected static final String serverFqdnOverride = "SERVER_FQDN_OVERRIDE";
    protected static final String checkHeaderLogPattern = "Checking {} header to determine the value of {}";
    protected String serverFqdn;
    protected String serverIpAddress;
    protected String[] prioritizedIdHeadersNames;
    protected String[] prioritizedPartnerHeadersNames;

    public MDCSetup() {
        this.prioritizedIdHeadersNames =
                new String[] {ONAPLogConstants.Headers.REQUEST_ID, Constants.HttpHeaders.HEADER_REQUEST_ID,
                        Constants.HttpHeaders.TRANSACTION_ID, Constants.HttpHeaders.ECOMP_REQUEST_ID};
        this.prioritizedPartnerHeadersNames =
                new String[] {HttpHeaders.AUTHORIZATION, ONAPLogConstants.Headers.PARTNER_NAME, HttpHeaders.USER_AGENT};
        initServerFqdnandIp();
    }

    public void setInstanceID() {
        MDC.put(ONAPLogConstants.MDCs.INSTANCE_UUID, INSTANCE_UUID);
    }

    protected void initServerFqdnandIp() {
        serverFqdn = getProperty(serverFqdnOverride);
        serverIpAddress = getProperty(serverIpAddressOverride);

        if (serverIpAddress.equals(Constants.DefaultValues.UNKNOWN)
                || serverFqdn.equals(Constants.DefaultValues.UNKNOWN)) {
            try {
                InetAddress addr = InetAddress.getLocalHost();
                if (serverFqdn.equals(Constants.DefaultValues.UNKNOWN)) {
                    serverFqdn = addr.getCanonicalHostName();
                }
                if (serverIpAddress.equals(Constants.DefaultValues.UNKNOWN)) {
                    serverIpAddress = addr.getHostAddress();
                }
            } catch (UnknownHostException e) {
                logger.trace("Cannot Resolve Host Name." + e.getMessage());
            }
        }
    }

    public void setServerFQDN() {
        MDC.put(ONAPLogConstants.MDCs.SERVER_FQDN, serverFqdn);
        MDC.put(ONAPLogConstants.MDCs.SERVER_IP_ADDRESS, serverIpAddress);
    }

    public void setClientIPAddress(HttpServletRequest httpServletRequest) {
        String clientIpAddress = "";
        if (httpServletRequest != null) {
            // This logic is to avoid setting the client ip address to that of the load
            // balancer in front of the application
            String getForwadedFor = httpServletRequest.getHeader("X-Forwarded-For");
            if (getForwadedFor != null) {
                clientIpAddress = getForwadedFor;
            } else {
                clientIpAddress = httpServletRequest.getRemoteAddr();
            }
        }
        MDC.put(ONAPLogConstants.MDCs.CLIENT_IP_ADDRESS, clientIpAddress);
    }

    public void setEntryTimeStamp() {
        MDC.put(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP,
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
    }

    public String getRequestId(SimpleMap headers) {
        String requestId = null;
        for (String headerName : this.prioritizedIdHeadersNames) {
            logger.trace(checkHeaderLogPattern, headerName, ONAPLogConstants.Headers.REQUEST_ID);
            requestId = headers.get(headerName);
            if (requestId != null && !requestId.isEmpty()) {
                return requestId;
            }
        }
        logger.trace("No valid requestId headers. Generating requestId: {}", requestId);
        return UUID.randomUUID().toString();
    }

    public void setInvocationId(SimpleMap headers) {
        String invocationId = headers.get(ONAPLogConstants.Headers.INVOCATION_ID);
        if (invocationId == null || invocationId.isEmpty())
            invocationId = UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.SERVER_INVOCATION_ID, invocationId);
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, invocationId);
    }

    public void setMDCPartnerName(SimpleMap headers) {
        String partnerName = getMDCPartnerName(headers);
        MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME, partnerName);
    }

    protected String getMDCPartnerName(SimpleMap headers) {
        String partnerName = null;
        for (String headerName : prioritizedPartnerHeadersNames) {
            logger.trace(checkHeaderLogPattern, headerName, ONAPLogConstants.MDCs.PARTNER_NAME);
            if (headerName.equals(HttpHeaders.AUTHORIZATION)) {
                partnerName = getBasicAuthUserName(headers);
            } else {
                partnerName = headers.get(headerName);
            }
            if (partnerName != null && !partnerName.isEmpty()) {
                return partnerName;
            }

        }
        logger.trace("{} value could not be determined, defaulting partnerName to {}.",
                ONAPLogConstants.MDCs.PARTNER_NAME, Constants.DefaultValues.UNKNOWN);
        return Constants.DefaultValues.UNKNOWN;
    }

    public void setLogTimestamp() {
        MDC.put(ONAPLogConstants.MDCs.LOG_TIMESTAMP,
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
    }

    public void setElapsedTime() {
        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
            ZonedDateTime entryTimestamp =
                    ZonedDateTime.parse(MDC.get(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP), timeFormatter);
            ZonedDateTime endTimestamp =
                    ZonedDateTime.parse(MDC.get(ONAPLogConstants.MDCs.LOG_TIMESTAMP), timeFormatter);

            MDC.put(ONAPLogConstants.MDCs.ELAPSED_TIME,
                    Long.toString(ChronoUnit.MILLIS.between(entryTimestamp, endTimestamp)));
        } catch (Exception e) {
            logger.trace("Unable to calculate elapsed time due to error: {}", e.getMessage());
        }
    }

    public void setElapsedTimeInvokeTimestamp() {
        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
            ZonedDateTime entryTimestamp =
                    ZonedDateTime.parse(MDC.get(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP), timeFormatter);
            ZonedDateTime endTimestamp =
                    ZonedDateTime.parse(MDC.get(ONAPLogConstants.MDCs.LOG_TIMESTAMP), timeFormatter);

            MDC.put(ONAPLogConstants.MDCs.ELAPSED_TIME,
                    Long.toString(ChronoUnit.MILLIS.between(entryTimestamp, endTimestamp)));
        } catch (Exception e) {
            logger.trace("Unable to calculate elapsed time due to error: {}", e.getMessage());
        }
    }

    public void setResponseStatusCode(int code) {
        String statusCode;
        if (Response.Status.Family.familyOf(code).equals(Response.Status.Family.SUCCESSFUL)) {
            statusCode = ONAPLogConstants.ResponseStatus.COMPLETE.toString();
        } else {
            statusCode = ONAPLogConstants.ResponseStatus.ERROR.toString();
            setErrorCode(code);
            setErrorDesc(code);
        }
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, statusCode);
    }

    public void setTargetEntity(ONAPComponentsList targetEntity) {
        MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, targetEntity.toString());
    }

    public void clearClientMDCs() {
        MDC.remove(ONAPLogConstants.MDCs.CLIENT_INVOCATION_ID);
        MDC.remove(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION);
        MDC.remove(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE);
        MDC.remove(ONAPLogConstants.MDCs.RESPONSE_CODE);
        MDC.remove(ONAPLogConstants.MDCs.TARGET_ENTITY);
        MDC.remove(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME);
        MDC.remove(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP);
        MDC.remove(ONAPLogConstants.MDCs.ERROR_CODE);
        MDC.remove(ONAPLogConstants.MDCs.ERROR_DESC);
    }

    public void setResponseDescription(int statusCode) {
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION, Response.Status.fromStatusCode(statusCode).toString());
    }

    public void setErrorCode(int statusCode) {
        MDC.put(ONAPLogConstants.MDCs.ERROR_CODE, String.valueOf(statusCode));
    }

    public void setErrorDesc(int statusCode) {
        MDC.put(ONAPLogConstants.MDCs.ERROR_DESC, Response.Status.fromStatusCode(statusCode).toString());
    }

    public String getProperty(String property) {
        String propertyValue = System.getProperty(property);
        if (propertyValue == null || propertyValue.isEmpty()) {
            propertyValue = System.getenv(property);
            if (propertyValue == null || propertyValue.isEmpty()) {
                propertyValue = Constants.DefaultValues.UNKNOWN;
            }
        }
        return propertyValue;
    }

    protected String getBasicAuthUserName(SimpleMap headers) {
        String encodedAuthorizationValue = headers.get(HttpHeaders.AUTHORIZATION);
        if (encodedAuthorizationValue != null && encodedAuthorizationValue.startsWith("Basic")) {
            try {
                // This will strip the word Basic and single space
                encodedAuthorizationValue = encodedAuthorizationValue.substring(6);
                byte[] decodedBytes = Base64.getDecoder().decode(encodedAuthorizationValue);
                String decodedString = new String(decodedBytes);
                int idx = decodedString.indexOf(':');
                return decodedString.substring(0, idx);
            } catch (IllegalArgumentException e) {
                logger.error("could not decode basic auth value " + encodedAuthorizationValue, e);
            }
        }
        return null;
    }
}
