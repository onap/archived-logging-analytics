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
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PayloadLoggingClientFilterTest {

    @Mock
    private ClientRequestContext requestContext;

    @Spy
    @InjectMocks
    private PayloadLoggingClientFilter payloadLoggingClientFilter;

    @Test
    public void formatMethodTest() throws IOException, URISyntaxException {
        when(requestContext.getHeaderString("X-HTTP-Method-Override")).thenReturn("filter");
        when(requestContext.getMethod()).thenReturn("filtered");
        String method = payloadLoggingClientFilter.formatMethod(requestContext);

        assertEquals("filtered (overridden to filter)", method);
    }

    @Test
    public void formatMethodNullHeaderTest() throws IOException, URISyntaxException {
        when(requestContext.getMethod()).thenReturn("filtered");
        String method = payloadLoggingClientFilter.formatMethod(requestContext);

        assertEquals("filtered", method);
    }

    @Test
    public void getHeadersTest() {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(Constants.HttpHeaders.ONAP_PARTNER_NAME, "SO");
        headers.add("Authorization", "Test");

        String printHeaders = payloadLoggingClientFilter.getHeaders(headers);

        assertEquals("{Authorization=[***REDACTED***], X-ONAP-PartnerName=[SO]}", printHeaders);
    }
}
