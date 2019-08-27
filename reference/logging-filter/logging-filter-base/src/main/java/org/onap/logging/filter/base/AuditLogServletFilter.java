/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.MDC;

public class AuditLogServletFilter extends AbstractAuditLogFilter<HttpServletRequest, HttpServletResponse>
        implements Filter {

    @Override
    public void destroy() {
        // this method does nothing
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException {
        MDCSetup mdcSetup = new MDCSetup();
        try {
            if (request != null && request instanceof HttpServletRequest) {
                pre((HttpServletRequest) request, mdcSetup);
            }
            filterChain.doFilter(request, response);
        } finally {
            if (request != null && request instanceof HttpServletRequest) {
                post((HttpServletRequest) request, (HttpServletResponse) response, mdcSetup);
            }
            MDC.clear();
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // this method does nothing
    }

    protected void pre(HttpServletRequest request, MDCSetup mdcSetup) {
        SimpleMap headers = new SimpleServletHeadersMap(request);
        pre(mdcSetup, headers, request, request);
    }

    @Override
    protected void setServiceName(HttpServletRequest request) {
        MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, request.getRequestURI());
    }

    private void post(HttpServletRequest request, HttpServletResponse response, MDCSetup mdcSetup) {
        post(mdcSetup, response);
    }

    @Override
    protected int getResponseCode(HttpServletResponse response) {
        return response.getStatus();
    }

}
