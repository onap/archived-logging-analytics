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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.logging.filter.base.Constants;
import org.onap.logging.filter.base.MDCSetup;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class SpringClientFilterTest {

    @Mock
    private MDCSetup mdcSetup;

    @Mock
    private ClientHttpResponse response;

    @Mock
    private HttpRequest clientRequest;

    @Mock
    private ClientHttpRequestExecution execution;

    @Spy
    @InjectMocks
    private SpringClientFilter springClientFilter;

    @After
    public void tearDown() {
        MDC.clear();
    }

    @Test
    public void processResponseTest() throws IOException {
        String partnerName = springClientFilter.getPartnerName();

        assertEquals("UNKNOWN", partnerName);
    }

    @Test
    public void extractTargetEntityTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.HttpHeaders.TARGET_ENTITY_HEADER, "SO");
        when(clientRequest.getHeaders()).thenReturn(headers);

        String targetEntity = springClientFilter.extractTargetEntity(clientRequest);
        assertEquals("SO", targetEntity);
    }

    @Test
    public void extractTargetEntityMDCTest() {
        MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, "SO");
        HttpHeaders headers = new HttpHeaders();
        when(clientRequest.getHeaders()).thenReturn(headers);

        String targetEntity = springClientFilter.extractTargetEntity(clientRequest);
        assertEquals("SO", targetEntity);
    }

    @Test
    public void extractTargetEntityNoHeaderTest() {
        HttpHeaders headers = new HttpHeaders();
        when(clientRequest.getHeaders()).thenReturn(headers);

        String targetEntity = springClientFilter.extractTargetEntity(clientRequest);
        assertEquals("Unknown-Target-Entity", targetEntity);
    }

    @Test
    public void setupMDCTest() throws URISyntaxException {
        doReturn("SO").when(springClientFilter).extractTargetEntity(clientRequest);
        URI uri = new URI("onap/so/serviceInstances");
        when(clientRequest.getURI()).thenReturn(uri);

        springClientFilter.setupMDC(clientRequest);

        assertEquals("onap/so/serviceInstances", MDC.get(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME));
        assertEquals("SO", MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals("INPROGRESS", MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
        assertNotNull(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE);
        assertNotNull(ONAPLogConstants.MDCs.SERVICE_NAME);
        assertNotNull(ONAPLogConstants.MDCs.SERVER_FQDN);
    }

    @Test
    public void setupHeadersTest() {
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, "8819bfb4-69d2-43fc-b0d6-81d2690533ea");
        HttpHeaders headers = new HttpHeaders();
        when(clientRequest.getHeaders()).thenReturn(headers);
        doReturn("0a908a5d-e774-4558-96ff-6edcbba65483").when(springClientFilter).extractRequestID(clientRequest);

        springClientFilter.setupHeaders(clientRequest);

        assertEquals("0a908a5d-e774-4558-96ff-6edcbba65483", headers.getFirst(ONAPLogConstants.Headers.REQUEST_ID));
        assertEquals("0a908a5d-e774-4558-96ff-6edcbba65483", headers.getFirst(Constants.HttpHeaders.HEADER_REQUEST_ID));
        assertEquals("0a908a5d-e774-4558-96ff-6edcbba65483", headers.getFirst(Constants.HttpHeaders.TRANSACTION_ID));
        assertEquals("0a908a5d-e774-4558-96ff-6edcbba65483", headers.getFirst(Constants.HttpHeaders.TRANSACTION_ID));
        assertEquals("0a908a5d-e774-4558-96ff-6edcbba65483", headers.getFirst(Constants.HttpHeaders.ECOMP_REQUEST_ID));
        assertEquals("8819bfb4-69d2-43fc-b0d6-81d2690533ea", headers.getFirst(ONAPLogConstants.Headers.INVOCATION_ID));
        assertEquals("UNKNOWN", headers.getFirst(ONAPLogConstants.Headers.PARTNER_NAME));
    }

    @Test
    public void extractRequestIDTest() {
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, "0a908a5d-e774-4558-96ff-6edcbba65483");
        String requestId = springClientFilter.extractRequestID(clientRequest);
        assertEquals("0a908a5d-e774-4558-96ff-6edcbba65483", requestId);
    }

    @Test
    public void extractRequestIDNullTest() {
        String requestId = springClientFilter.extractRequestID(clientRequest);
        assertNotNull(requestId);
        assertNotNull(ONAPLogConstants.MDCs.LOG_TIMESTAMP);
        assertNotNull(ONAPLogConstants.MDCs.ELAPSED_TIME);
    }

    @Test
    public void interceptTest() throws IOException {
        byte[] body = new byte[3];
        doNothing().when(springClientFilter).processRequest(clientRequest, body);
        doReturn(response).when(execution).execute(clientRequest, body);
        doNothing().when(springClientFilter).processResponse(response);

        ClientHttpResponse httpResponse = springClientFilter.intercept(clientRequest, body, execution);
        assertEquals(response, httpResponse);
    }
}
