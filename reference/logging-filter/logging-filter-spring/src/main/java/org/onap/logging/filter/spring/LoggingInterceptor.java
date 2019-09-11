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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Providers;
import org.onap.logging.filter.base.AbstractAuditLogFilter;
import org.onap.logging.filter.base.SimpleMap;
import org.onap.logging.filter.base.SimpleServletHeadersMap;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LoggingInterceptor extends AbstractAuditLogFilter<HttpServletRequest, HttpServletResponse>
        implements HandlerInterceptor {

    @Context
    private Providers providers;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        SimpleMap headers = new SimpleServletHeadersMap(request);
        pre(headers, request, request);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        post(response);
    }

    @Override
    protected int getResponseCode(HttpServletResponse response) {
        return response.getStatus();
    }

    @Override
    protected void setServiceName(HttpServletRequest request) {
        MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, request.getRequestURI());
    }

}
