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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Providers;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Priority(0)
public class MetricLogClientFilter implements ClientRequestFilter, ClientResponseFilter {

    @Context
    private Providers providers;
    private MDCSetup mdcSetup = new MDCSetup();
    private static final String TRACE = "trace-#";
    private static Logger logger = LoggerFactory.getLogger(MetricLogClientFilter.class);
    private final String partnerName;
    private static final Marker INVOKE_RETURN = MarkerFactory.getMarker("INVOKE-RETURN");

    public MetricLogClientFilter() {
        partnerName = getPartnerName();
    }

    @Override
    public void filter(ClientRequestContext clientRequest) {
        try {
            setupMDC(clientRequest);
            setupHeaders(clientRequest);
            logger.info(ONAPLogConstants.Markers.INVOKE, "Invoke");
        } catch (Exception e) {
            logger.warn("Error in JAX-RS client request filter", e);
        }
    }

    protected void setupHeaders(ClientRequestContext clientRequest) {
        MultivaluedMap<String, Object> headers = clientRequest.getHeaders();
        String requestId = extractRequestID(clientRequest);
        headers.add(ONAPLogConstants.Headers.REQUEST_ID, requestId);
        headers.add(Constants.HttpHeaders.HEADER_REQUEST_ID, requestId);
        headers.add(Constants.HttpHeaders.TRANSACTION_ID, requestId);
        headers.add(Constants.HttpHeaders.ECOMP_REQUEST_ID, requestId);
        headers.add(ONAPLogConstants.Headers.INVOCATION_ID, MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID));
        headers.add(ONAPLogConstants.Headers.PARTNER_NAME, partnerName);
    }

    protected void setupMDC(ClientRequestContext clientRequest) {
        MDC.put(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP,
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
        MDC.put(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME, clientRequest.getUri().toString());
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.INPROGRESS.toString());
        mdcSetup.setInvocationIdFromMDC();
        String targetEntity = MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY);
        if (targetEntity != null) {
            MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, targetEntity);
        } else {
            MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, Constants.DefaultValues.UNKNOWN_TARGET_ENTITY);
        }
        if (MDC.get(ONAPLogConstants.MDCs.SERVICE_NAME) == null) {
            MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, clientRequest.getUri().getPath());
        }
        mdcSetup.setServerFQDN();
    }

    protected String extractRequestID(ClientRequestContext clientRequest) {
        String requestId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
        if (requestId == null || requestId.isEmpty() || requestId.equals(TRACE)) {
            requestId = UUID.randomUUID().toString();
            mdcSetup.setLogTimestamp();
            mdcSetup.setElapsedTimeInvokeTimestamp();
            logger.warn("Could not Find Request ID Generating New One: {}", clientRequest.getUri().getPath());
            MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, requestId);
        }
        return requestId;
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        try {
            mdcSetup.setLogTimestamp();
            mdcSetup.setElapsedTimeInvokeTimestamp();
            mdcSetup.setResponseStatusCode(responseContext.getStatus());
            mdcSetup.setResponseDescription(responseContext.getStatus());
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, String.valueOf(responseContext.getStatus()));
            logger.info(INVOKE_RETURN, "InvokeReturn");
            mdcSetup.clearClientMDCs();
        } catch (Exception e) {
            logger.warn("Error in JAX-RS request,response client filter", e);
        }
    }

    protected String getPartnerName() {
        PropertyUtil p = new PropertyUtil();
        return p.getProperty(Constants.Property.PARTNER_NAME);
    }
}
