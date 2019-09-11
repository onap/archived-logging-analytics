/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.MDC;

@RunWith(MockitoJUnitRunner.class)
public class MetricLogClientFilterTest {
    @Mock
    private ClientRequestContext clientRequest;

    @Spy
    @InjectMocks
    private MetricLogClientFilter metricLogClientFilter;

    @After
    public void tearDown() {
        MDC.clear();
    }

    @Test
    public void setupHeadersTest() {
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, "8819bfb4-69d2-43fc-b0d6-81d2690533ea");
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        doReturn("0a908a5d-e774-4558-96ff-6edcbba65483").when(metricLogClientFilter).extractRequestID();

        metricLogClientFilter.setupHeaders(clientRequest, headers);

        assertEquals("0a908a5d-e774-4558-96ff-6edcbba65483", headers.getFirst(ONAPLogConstants.Headers.REQUEST_ID));
        assertEquals("0a908a5d-e774-4558-96ff-6edcbba65483", headers.getFirst(Constants.HttpHeaders.HEADER_REQUEST_ID));
        assertEquals("0a908a5d-e774-4558-96ff-6edcbba65483", headers.getFirst(Constants.HttpHeaders.TRANSACTION_ID));
        assertEquals("0a908a5d-e774-4558-96ff-6edcbba65483", headers.getFirst(Constants.HttpHeaders.TRANSACTION_ID));
        assertEquals("0a908a5d-e774-4558-96ff-6edcbba65483", headers.getFirst(Constants.HttpHeaders.ECOMP_REQUEST_ID));
        assertEquals("8819bfb4-69d2-43fc-b0d6-81d2690533ea", headers.getFirst(ONAPLogConstants.Headers.INVOCATION_ID));
        assertEquals("UNKNOWN", headers.getFirst(ONAPLogConstants.Headers.PARTNER_NAME));
    }

    @Test
    public void setupMDCTest() throws URISyntaxException {
        // TODO ingest change from upstream
        MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, "SO");
        URI uri = new URI("onap/so/serviceInstances");
        doReturn(uri).when(clientRequest).getUri();

        metricLogClientFilter.setupMDC(clientRequest);

        assertEquals("onap/so/serviceInstances", MDC.get(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME));
        assertEquals("SO", MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals("INPROGRESS", MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
        assertNotNull(ONAPLogConstants.MDCs.SERVICE_NAME);
        assertNotNull(ONAPLogConstants.MDCs.SERVER_FQDN);
        assertNotNull(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE);
    }

    @Test
    public void setupMDCNoTargetEntityTest() throws URISyntaxException {
        URI uri = new URI("onap/so/serviceInstances");
        doReturn(uri).when(clientRequest).getUri();

        metricLogClientFilter.setupMDC(clientRequest);

        assertEquals("onap/so/serviceInstances", MDC.get(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME));
        assertEquals("Unknown-Target-Entity", MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals("INPROGRESS", MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
        assertNotNull(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE);
    }

    @Test
    public void extractRequestIDTest() {
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, "0a908a5d-e774-4558-96ff-6edcbba65483");
        String requestId = metricLogClientFilter.extractRequestID();
        assertEquals("0a908a5d-e774-4558-96ff-6edcbba65483", requestId);
    }

    @Test
    public void extractRequestIDNullTest() throws URISyntaxException {
        MDC.put(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP,
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
        String requestId = metricLogClientFilter.extractRequestID();
        assertNotNull(requestId);
        assertNotNull(ONAPLogConstants.MDCs.LOG_TIMESTAMP);
        assertNotNull(ONAPLogConstants.MDCs.ELAPSED_TIME);

    }
}
