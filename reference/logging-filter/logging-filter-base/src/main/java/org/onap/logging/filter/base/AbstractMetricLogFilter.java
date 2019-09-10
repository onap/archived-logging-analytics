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
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public abstract class AbstractMetricLogFilter<Request, Response, RequestHeaders> extends AbstractFilter {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractMetricLogFilter.class);
    private final String partnerName;
    private static final Marker INVOKE_RETURN = MarkerFactory.getMarker("INVOKE-RETURN");

    public AbstractMetricLogFilter() {
        partnerName = getPartnerName();
    }

    protected abstract void addHeader(RequestHeaders requestHeaders, String headerName, String headerValue);

    protected abstract String getTargetServiceName(Request request);

    protected abstract String getServiceName(Request request);

    protected abstract int getHttpStatusCode(Response response);

    protected abstract String getResponseCode(Response response);

    protected abstract String getTargetEntity(Request request);

    protected void pre(Request request, RequestHeaders requestHeaders) {
        try {
            setupMDC(request);
            setupHeaders(request, requestHeaders);
            logger.info(ONAPLogConstants.Markers.INVOKE, "Invoke");
        } catch (Exception e) {
            logger.warn("Error in AbstractMetricLogFilter pre", e);
        }
    }

    protected void setupHeaders(Request clientRequest, RequestHeaders requestHeaders) {
        String requestId = extractRequestID();
        addHeader(requestHeaders, ONAPLogConstants.Headers.REQUEST_ID, requestId);
        addHeader(requestHeaders, Constants.HttpHeaders.HEADER_REQUEST_ID, requestId);
        addHeader(requestHeaders, Constants.HttpHeaders.TRANSACTION_ID, requestId);
        addHeader(requestHeaders, Constants.HttpHeaders.ECOMP_REQUEST_ID, requestId);
        addHeader(requestHeaders, ONAPLogConstants.Headers.INVOCATION_ID, MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID));
        addHeader(requestHeaders, ONAPLogConstants.Headers.PARTNER_NAME, partnerName);
    }

    protected void setupMDC(Request request) {
        MDC.put(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP,
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
        MDC.put(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME, getTargetServiceName(request));
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.INPROGRESS.toString());
        setInvocationIdFromMDC();

        if (MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY) == null) {
            String targetEntity = getTargetEntity(request);
            if (targetEntity != null) {
                MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, targetEntity);
            } else {
                MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, Constants.DefaultValues.UNKNOWN_TARGET_ENTITY);
            }
        }

        if (MDC.get(ONAPLogConstants.MDCs.SERVICE_NAME) == null) {
            MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, getServiceName(request));
        }
        setServerFQDN();
    }

    protected String extractRequestID() {
        String requestId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
            setLogTimestamp();
            setElapsedTimeInvokeTimestamp();
            logger.warn("No value found in MDC when checking key {} value will be set to {}",
                    ONAPLogConstants.MDCs.REQUEST_ID, requestId);
            MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, requestId);
        }
        return requestId;
    }

    protected void post(Request request, Response response) {
        try {
            setLogTimestamp();
            setElapsedTimeInvokeTimestamp();
            setResponseStatusCode(getHttpStatusCode(response));
            setResponseDescription(getHttpStatusCode(response));
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, getResponseCode(response));
            logger.info(INVOKE_RETURN, "InvokeReturn");
            clearClientMDCs();
        } catch (Exception e) {
            logger.warn("Error in AbstractMetricLogFilter post", e);
        }
    }

    protected String getPartnerName() {
        return getProperty(Constants.Property.PARTNER_NAME);
    }

}
