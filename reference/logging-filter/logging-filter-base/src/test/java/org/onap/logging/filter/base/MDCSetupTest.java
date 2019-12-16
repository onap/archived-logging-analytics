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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.MDC;

@RunWith(MockitoJUnitRunner.class)
public class MDCSetupTest extends MDCSetup {

    @Mock
    private HttpServletRequest httpServletRequest;

    private String requestId = "4d31fe02-4918-4975-942f-fe51a44e6a9b";
    private String invocationId = "4d31fe02-4918-4975-942f-fe51a44e6a9a";

    @After
    public void tearDown() {
        MDC.clear();
        System.clearProperty("partnerName");
    }

    @Test
    public void setElapsedTimeTest() {
        String expected = "318";
        MDC.put(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP, "2019-06-18T02:09:06.024Z");
        MDC.put(ONAPLogConstants.MDCs.LOG_TIMESTAMP, "2019-06-18T02:09:06.342Z");

        setElapsedTime();
        assertEquals(expected, MDC.get(ONAPLogConstants.MDCs.ELAPSED_TIME));
    }

    @Test
    public void setElapsedTimeInvokeTimestampTest() {
        String expected = "318";
        MDC.put(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP, "2019-06-18T02:09:06.024Z");
        MDC.put(ONAPLogConstants.MDCs.LOG_TIMESTAMP, "2019-06-18T02:09:06.342Z");

        setElapsedTimeInvokeTimestamp();
        assertEquals(expected, MDC.get(ONAPLogConstants.MDCs.ELAPSED_TIME));
    }

    @Test
    public void setRequestIdTest() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(ONAPLogConstants.Headers.REQUEST_ID, requestId);
        String fetchedRequestId = getRequestId(new SimpleHashMap(headers));
        assertEquals(requestId, fetchedRequestId);
    }

    @Test
    public void setRequestIdRequestIdHeaderTest() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(Constants.HttpHeaders.HEADER_REQUEST_ID, requestId);
        String fetchedRequestId = getRequestId(new SimpleHashMap(headers));
        assertEquals(requestId, fetchedRequestId);
    }

    @Test
    public void setRequestIdTransactionIdHeaderTest() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(Constants.HttpHeaders.TRANSACTION_ID, requestId);
        String fetchedRequestId = getRequestId(new SimpleHashMap(headers));
        assertEquals(requestId, fetchedRequestId);
    }

    @Test
    public void setRequestIdEcompRequestIdHeaderTest() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(Constants.HttpHeaders.ECOMP_REQUEST_ID, requestId);
        String fetchedRequestId = getRequestId(new SimpleHashMap(headers));
        assertEquals(requestId, fetchedRequestId);
    }

    @Test
    public void setRequestIdNoHeaderTest() {
        HashMap<String, String> headers = new HashMap<>();
        String fetchedRequestId = getRequestId(new SimpleHashMap(headers));
        assertNotNull(fetchedRequestId);
    }

    @Test
    public void setInvocationIdTest() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(ONAPLogConstants.Headers.INVOCATION_ID, invocationId);
        setInvocationId(new SimpleHashMap(headers));
        assertEquals(invocationId, MDC.get(ONAPLogConstants.MDCs.SERVER_INVOCATION_ID));
        assertEquals(invocationId, MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID));
    }

    @Test
    public void setInvocationIdNoHeaderTest() {
        HashMap<String, String> headers = new HashMap<>();
        setInvocationId(new SimpleHashMap(headers));
        assertNotNull(MDC.get(ONAPLogConstants.MDCs.SERVER_INVOCATION_ID));
        assertNotNull(MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID));
    }

    @Test
    public void setResponseStatusCodeTest() {
        setResponseStatusCode(200);
        assertEquals("COMPLETE", MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
    }

    @Test
    public void setResponseStatusCodeErrorTest() {
        setResponseStatusCode(400);
        assertEquals("ERROR", MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
        assertEquals("400", MDC.get(ONAPLogConstants.MDCs.ERROR_CODE));
        assertEquals("Bad Request", MDC.get(ONAPLogConstants.MDCs.ERROR_DESC));
    }

    @Test
    public void clearClientMDCsTest() {
        MDC.put(ONAPLogConstants.MDCs.CLIENT_INVOCATION_ID, "7b77143c-9b50-410c-ac2f-05758a68e3e9");
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION, "Bad Gateway");
        MDC.put(ONAPLogConstants.MDCs.ERROR_DESC, "Bad Gateway");
        MDC.put(ONAPLogConstants.MDCs.ERROR_CODE, "502");
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, "502");
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, "502");
        MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, "SO");
        MDC.put(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME, "SDNC");
        MDC.put(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP, "2019-06-18T02:09:06.024Z");

        clearClientMDCs();
        assertNull(MDC.get(ONAPLogConstants.MDCs.CLIENT_INVOCATION_ID));
        assertNull(MDC.get(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION));
        assertNull(MDC.get(ONAPLogConstants.MDCs.ERROR_CODE));
        assertNull(MDC.get(ONAPLogConstants.MDCs.ERROR_DESC));
        assertNull(MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
        assertNull(MDC.get(ONAPLogConstants.MDCs.RESPONSE_CODE));
        assertNull(MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertNull(MDC.get(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME));
        assertNull(MDC.get(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP));
    }

    @Test
    public void setTargetEntityTest() {
        setTargetEntity(ONAPComponents.SO);
        assertEquals("SO", MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
    }

    @Test
    public void setResponseDescriptionTest() {
        setResponseDescription(502);
        assertEquals("Bad Gateway", MDC.get(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION));
    }

    @Test
    public void setMDCPartnerNameBearerToken() {
        MultivaluedMap<String, String> headerMap = new MultivaluedHashMap<>();
        String value = "Bearer some-compex-token";
        headerMap.putSingle(HttpHeaders.AUTHORIZATION, value);
        SimpleMap headers = new SimpleJaxrsHeadersMap(headerMap);

        setMDCPartnerName(headers);

        assertEquals(Constants.DefaultValues.UNKNOWN, MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME));
    }

    @Test
    public void setMDCPartnerNameFromBasicAuth() {
        MultivaluedMap<String, String> headerMap = new MultivaluedHashMap<>();
        String value = "Basic dXNlcjpwYXNz"; // decodes to user:pass
        headerMap.putSingle(HttpHeaders.AUTHORIZATION, value);
        SimpleMap headers = new SimpleJaxrsHeadersMap(headerMap);

        setMDCPartnerName(headers);

        assertEquals("user", MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME));
    }

    @Test
    public void setMDCPartnerNameTest() {
        MultivaluedMap<String, String> headerMap = new MultivaluedHashMap<>();
        headerMap.putSingle(ONAPLogConstants.Headers.PARTNER_NAME, "SO");
        SimpleMap headers = new SimpleJaxrsHeadersMap(headerMap);

        setMDCPartnerName(headers);

        assertEquals("SO", MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME));
    }

    @Test
    public void setMDCPartnerNameUserAgentHeaderTest() {
        MultivaluedMap<String, String> headerMap = new MultivaluedHashMap<>();
        headerMap.putSingle(HttpHeaders.USER_AGENT, "Apache-HttpClient/4.5.8 (Java/1.8.0_191)");
        SimpleMap headers = new SimpleJaxrsHeadersMap(headerMap);

        setMDCPartnerName(headers);

        assertEquals("Apache-HttpClient/4.5.8 (Java/1.8.0_191)", MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME));
    }

    @Test
    public void setMDCPartnerNameNoHeaderTest() {
        MultivaluedMap<String, String> headerMap = new MultivaluedHashMap<>();
        SimpleMap headers = new SimpleJaxrsHeadersMap(headerMap);

        setMDCPartnerName(headers);

        assertEquals("UNKNOWN", MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME));
    }

    @Test
    public void setServerFQDNTest() {
        setServerFQDN();
        assertNotNull(MDC.get(ONAPLogConstants.MDCs.SERVER_IP_ADDRESS));
        assertNotNull(MDC.get(ONAPLogConstants.MDCs.SERVER_FQDN));
    }

    @Test
    public void setClientIPAddressTest() {
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("127.0.0.2");
        setClientIPAddress(httpServletRequest);

        assertEquals("127.0.0.2", MDC.get(ONAPLogConstants.MDCs.CLIENT_IP_ADDRESS));
    }

    @Test
    public void setClientIPAddressNoHeaderTest() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        setClientIPAddress(httpServletRequest);

        assertEquals("127.0.0.1", MDC.get(ONAPLogConstants.MDCs.CLIENT_IP_ADDRESS));
    }

    @Test
    public void setClientIPAddressNullTest() {
        setClientIPAddress(null);

        assertEquals("", MDC.get(ONAPLogConstants.MDCs.CLIENT_IP_ADDRESS));
    }

    @Test
    public void setEntryTimeStampTest() {
        setEntryTimeStamp();

        assertNotNull(MDC.get(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP));
    }

    @Test
    public void setLogTimestampTest() {
        setLogTimestamp();

        assertNotNull(MDC.get(ONAPLogConstants.MDCs.LOG_TIMESTAMP));
    }

    @Test
    public void setInstanceIDTest() {
        setInstanceID();

        assertNotNull(MDC.get(ONAPLogConstants.MDCs.INSTANCE_UUID));
    }

    @Test
    public void getPropertyTest() {
        System.setProperty("partnerName", "partnerName");

        String partnerName = getProperty("partnerName");
        assertEquals("partnerName", partnerName);
    }

    @Test
    public void getPropertyNullTest() {
        String partnerName = getProperty("partner");
        assertEquals("UNKNOWN", partnerName);
    }

}
