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

    public void setInstanceID() {
        MDC.put(ONAPLogConstants.MDCs.INSTANCE_UUID, INSTANCE_UUID);
    }

    public void setServerFQDN() {
        String serverFQDN = "";
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
            serverFQDN = addr.getCanonicalHostName();
            MDC.put(ONAPLogConstants.MDCs.SERVER_IP_ADDRESS, addr.getHostAddress());
        } catch (UnknownHostException e) {
            logger.warn("Cannot Resolve Host Name");
            serverFQDN = "";
        }
        MDC.put(ONAPLogConstants.MDCs.SERVER_FQDN, serverFQDN);
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
        logger.trace("Checking X-ONAP-RequestID header for requestId.");
        String requestId = headers.get(ONAPLogConstants.Headers.REQUEST_ID);
        if (requestId != null && !requestId.isEmpty()) {
            return requestId;
        }

        logger.trace("No valid X-ONAP-RequestID header value. Checking X-RequestID header for requestId.");
        requestId = headers.get(Constants.HttpHeaders.HEADER_REQUEST_ID);
        if (requestId != null && !requestId.isEmpty()) {
            return requestId;
        }

        logger.trace("No valid X-RequestID header value. Checking X-TransactionID header for requestId.");
        requestId = headers.get(Constants.HttpHeaders.TRANSACTION_ID);
        if (requestId != null && !requestId.isEmpty()) {
            return requestId;
        }

        logger.trace("No valid X-TransactionID header value. Checking X-ECOMP-RequestID header for requestId.");
        requestId = headers.get(Constants.HttpHeaders.ECOMP_REQUEST_ID);
        if (requestId != null && !requestId.isEmpty()) {
            return requestId;
        }

        logger.trace("No valid requestId headers. Generating requestId: {}", requestId);
        return UUID.randomUUID().toString();
    }

    public void setInvocationId(SimpleMap headers) {
        String invocationId = headers.get(ONAPLogConstants.Headers.INVOCATION_ID);
        if (invocationId == null || invocationId.isEmpty())
            invocationId = UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, invocationId);
    }

    public void setInvocationIdFromMDC() {
        String invocationId = MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID);
        if (invocationId == null || invocationId.isEmpty())
            invocationId = UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, invocationId);
    }

    public void setMDCPartnerName(SimpleMap headers) {
        String partnerName = getMDCPartnerName(headers);
        MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME, partnerName);
    }

    protected String getMDCPartnerName(SimpleMap headers) {
        String checkHeaderLogPattern = "Checking {} header to determine the value of {}";

        logger.trace(checkHeaderLogPattern, HttpHeaders.AUTHORIZATION, ONAPLogConstants.MDCs.PARTNER_NAME);
        String partnerName = getBasicAuthUserName(headers);
        if (partnerName != null && !partnerName.isEmpty()) {
            return partnerName;
        }

        logger.trace(checkHeaderLogPattern, ONAPLogConstants.Headers.PARTNER_NAME, ONAPLogConstants.MDCs.PARTNER_NAME);
        partnerName = headers.get(ONAPLogConstants.Headers.PARTNER_NAME);
        if (partnerName != null && !partnerName.isEmpty()) {
            return partnerName;
        }

        logger.trace(checkHeaderLogPattern, HttpHeaders.USER_AGENT, ONAPLogConstants.MDCs.PARTNER_NAME);
        partnerName = headers.get(HttpHeaders.USER_AGENT);
        if (partnerName != null && !partnerName.isEmpty()) {
            return partnerName;
        }

        logger.trace(checkHeaderLogPattern, Constants.HttpHeaders.CLIENT_ID, ONAPLogConstants.MDCs.PARTNER_NAME);
        partnerName = headers.get(Constants.HttpHeaders.CLIENT_ID);
        if (partnerName != null && !partnerName.isEmpty()) {
            return partnerName;
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
            logger.warn("Unable to calculate elapsed time due to error: {}", e.getMessage());
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
            logger.warn("Unable to calculate elapsed time due to error: {}", e.getMessage());
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
        MDC.remove(ONAPLogConstants.MDCs.INVOCATION_ID);
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
        logger.info("Checking for system property [{}]", property);
        String propertyValue = System.getProperty(property);
        if (propertyValue == null || propertyValue.isEmpty()) {
            logger.info("System property was null or empty. Checking environment variable for: {}", property);
            propertyValue = System.getenv(property);
            if (propertyValue == null || propertyValue.isEmpty()) {
                logger.info("Environment variable: {} was null or empty. Returning value: {}", property,
                        Constants.DefaultValues.UNKNOWN);
                propertyValue = Constants.DefaultValues.UNKNOWN;
            }
        }
        return propertyValue;
    }

    protected String getBasicAuthUserName(SimpleMap headers) {
        String encodedAuthorizationValue = headers.get(HttpHeaders.AUTHORIZATION);
        if (encodedAuthorizationValue != null) {
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
