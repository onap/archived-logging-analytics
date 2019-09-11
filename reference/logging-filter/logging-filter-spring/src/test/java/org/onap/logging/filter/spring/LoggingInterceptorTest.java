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

package org.onap.logging.filter.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.MDC;

@RunWith(MockitoJUnitRunner.class)
public class LoggingInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @Spy
    @InjectMocks
    private LoggingInterceptor loggingInterceptor;

    @Test
    public void preHandleTest() throws Exception {
        loggingInterceptor.preHandle(request, response, handler);
        assertEquals(MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE), "INPROGRESS");
        assertNotNull(MDC.get(ONAPLogConstants.MDCs.REQUEST_ID));
    }
}
