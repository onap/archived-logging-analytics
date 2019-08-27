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

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Providers;
import org.onap.logging.filter.base.MDCSetup;
import org.onap.logging.filter.base.SimpleMap;
import org.onap.logging.filter.base.SimpleServletHeadersMap;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class LoggingInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    private MDCSetup mdcSetup = new MDCSetup();

    @Context
    private Providers providers;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        SimpleMap headers = new SimpleServletHeadersMap(request);
        String requestId = mdcSetup.getRequestId(headers);
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, requestId);
        mdcSetup.setInvocationId(headers);
        mdcSetup.setServiceName(request);
        mdcSetup.setMDCPartnerName(headers);
        mdcSetup.setClientIPAddress(request);
        mdcSetup.setEntryTimeStamp();
        mdcSetup.setInstanceID();
        mdcSetup.setServerFQDN();
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.INPROGRESS.toString());
        mdcSetup.setLogTimestamp();
        mdcSetup.setElapsedTime();
        logger.info(ONAPLogConstants.Markers.ENTRY, "Entering");
        if (logger.isDebugEnabled())
            logRequestInformation(request);
        return true;
    }

    protected void logRequestInformation(HttpServletRequest request) {
        Map<String, String> headers = Collections.list((request).getHeaderNames()).stream()
                .collect(Collectors.toMap(h -> h, request::getHeader));

        logger.debug("===========================request begin================================================");
        logger.debug("URI         : {}", request.getRequestURI());
        logger.debug("Method      : {}", request.getMethod());
        logger.debug("Headers     : {}", headers);
        logger.debug("==========================request end================================================");

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        mdcSetup.setResponseStatusCode(response.getStatus());
        mdcSetup.setLogTimestamp();
        mdcSetup.setElapsedTime();
        mdcSetup.setResponseDescription(response.getStatus());
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, String.valueOf(response.getStatus()));
        logger.info(ONAPLogConstants.Markers.EXIT, "Exiting.");
        MDC.clear();
    }
}
