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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.UriInfo;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.logging.filter.base.AuditLogContainerFilter;
import org.onap.logging.filter.base.MDCSetup;
import org.onap.logging.filter.base.SimpleMap;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.MDC;

@RunWith(MockitoJUnitRunner.class)
public class AuditLogContainerFilterTest {

    @Mock
    private ContainerRequestContext containerRequest;

    @Mock
    private ContainerResponseContext containerResponse;

    @Mock
    private MDCSetup mdcSetup;

    @Mock
    private UriInfo uriInfo;

    @Spy
    @InjectMocks
    private AuditLogContainerFilter auditLogContainerFilter;

    @After
    public void tearDown() {
        MDC.clear();
    }

    @Test
    public void filterTest() {
        when(mdcSetup.getRequestId(any(SimpleMap.class))).thenReturn("e3b08fa3-535f-4c1b-8228-91318d2bb4ee");
        when(uriInfo.getPath()).thenReturn("onap/so/serviceInstances");
        doReturn(uriInfo).when(containerRequest).getUriInfo();
        auditLogContainerFilter.filter(containerRequest);

        assertEquals("e3b08fa3-535f-4c1b-8228-91318d2bb4ee", MDC.get(ONAPLogConstants.MDCs.REQUEST_ID));
        assertEquals("onap/so/serviceInstances", MDC.get(ONAPLogConstants.MDCs.SERVICE_NAME));
        assertEquals("INPROGRESS", MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
    }

    @Test
    public void getResponseCodeTest() {
        when(containerResponse.getStatus()).thenReturn(200);
        int responseCode = auditLogContainerFilter.getResponseCode(containerResponse);

        assertEquals(200, responseCode);
    }
}
