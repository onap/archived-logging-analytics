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

package org.onap.logging.filter.spring;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.onap.logging.filter.base.Constants;
import org.onap.logging.filter.base.MDCSetup;
import org.onap.logging.filter.base.PropertyUtil;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

public class SpringClientFilter implements ClientHttpRequestInterceptor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String TRACE = "trace-#";
    private MDCSetup mdcSetup = new MDCSetup();
    private final String partnerName;
    private static final Marker INVOKE_RETURN = MarkerFactory.getMarker("INVOKE-RETURN");

    public SpringClientFilter() {
        this.partnerName = getPartnerName();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        processRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        processResponse(response);
        return response;
    }

    protected void processRequest(HttpRequest request, byte[] body) throws IOException {
        mdcSetup.setInvocationIdFromMDC();
        setupMDC(request);
        setupHeaders(request);
        if (logger.isDebugEnabled()) {
            logger.debug("===========================request begin================================================");
            logger.debug("URI         : {}", request.getURI());
            logger.debug("Method      : {}", request.getMethod());
            logger.debug("Headers     : {}", request.getHeaders());
            logger.debug("Request body: {}", new String(body, StandardCharsets.UTF_8));
            logger.debug("==========================request end================================================");
        }
    }

    protected void setupHeaders(HttpRequest clientRequest) {
        HttpHeaders headers = clientRequest.getHeaders();
        String requestId = extractRequestID(clientRequest);
        headers.add(ONAPLogConstants.Headers.REQUEST_ID, requestId);
        headers.add(Constants.HttpHeaders.HEADER_REQUEST_ID, requestId);
        headers.add(Constants.HttpHeaders.TRANSACTION_ID, requestId);
        headers.add(Constants.HttpHeaders.ECOMP_REQUEST_ID, requestId);
        headers.add(ONAPLogConstants.Headers.INVOCATION_ID, MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID));
        headers.add(ONAPLogConstants.Headers.PARTNER_NAME, partnerName);
    }

    protected String extractRequestID(HttpRequest clientRequest) {
        String requestId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
        if (requestId == null || requestId.isEmpty() || requestId.equals(TRACE)) {
            requestId = UUID.randomUUID().toString();
            mdcSetup.setLogTimestamp();
            mdcSetup.setElapsedTimeInvokeTimestamp();
            logger.warn("Could not Find Request ID Generating New One: {}", clientRequest.getURI());
            MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, requestId);
        }
        return requestId;
    }

    protected void setupMDC(HttpRequest clientRequest) {
        MDC.put(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP,
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
        MDC.put(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME, clientRequest.getURI().toString());
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.INPROGRESS.toString());
        MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, extractTargetEntity(clientRequest));
        if (MDC.get(ONAPLogConstants.MDCs.SERVICE_NAME) == null) {
            MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, clientRequest.getURI().getPath());
        }
        mdcSetup.setServerFQDN();
    }

    protected String extractTargetEntity(HttpRequest clientRequest) {
        HttpHeaders headers = clientRequest.getHeaders();
        String headerTargetEntity = null;
        List<String> headerTargetEntityList = headers.get(Constants.HttpHeaders.TARGET_ENTITY_HEADER);
        if (headerTargetEntityList != null && !headerTargetEntityList.isEmpty())
            headerTargetEntity = headerTargetEntityList.get(0);
        String targetEntity = MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY);
        if (targetEntity != null && !targetEntity.isEmpty()) {
            return targetEntity;
        } else if (headerTargetEntity != null && !headerTargetEntity.isEmpty()) {
            targetEntity = headerTargetEntity;
        } else {
            targetEntity = Constants.DefaultValues.UNKNOWN_TARGET_ENTITY;
            logger.warn("Could not Target Entity: {}", clientRequest.getURI());
        }
        return targetEntity;
    }

    protected void processResponse(ClientHttpResponse response) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("============================response begin==========================================");
            logger.debug("Status code  : {}", response.getStatusCode());
            logger.debug("Status text  : {}", response.getStatusText());
            logger.debug("Headers      : {}", response.getHeaders());
            logger.debug("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
            logger.debug("=======================response end=================================================");
        }
        mdcSetup.setLogTimestamp();
        mdcSetup.setElapsedTimeInvokeTimestamp();
        mdcSetup.setResponseStatusCode(response.getRawStatusCode());
        int statusCode = response.getRawStatusCode();
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, String.valueOf(statusCode));
        mdcSetup.setResponseDescription(statusCode);
        logger.info(INVOKE_RETURN, "InvokeReturn");
        mdcSetup.clearClientMDCs();
    }

    protected String getPartnerName() {
        PropertyUtil p = new PropertyUtil();
        return p.getProperty(Constants.Property.PARTNER_NAME);
    }
}
