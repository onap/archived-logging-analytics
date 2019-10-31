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
import java.util.List;
import org.onap.logging.filter.base.AbstractMetricLogFilter;
import org.onap.logging.filter.base.Constants;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class SpringClientFilter extends AbstractMetricLogFilter<HttpRequest, ClientHttpResponse, HttpHeaders>
        implements ClientHttpRequestInterceptor {

    public SpringClientFilter() {

    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        pre(request, request.getHeaders());
        ClientHttpResponse response = execution.execute(request, body);
        post(request, response);
        return response;
    }

    @Override
    protected void addHeader(HttpHeaders requestHeaders, String headerName, String headerValue) {
        requestHeaders.add(headerName, headerValue);
    }

    @Override
    protected String getTargetServiceName(HttpRequest request) {
        return request.getURI().toString();
    }

    @Override
    protected int getHttpStatusCode(ClientHttpResponse response) {
        try {
            return response.getStatusCode().value();
        } catch (IOException e) {
            // TODO figure out the right thing to do here
            return 500;
        }
    }

    @Override
    protected String getResponseCode(ClientHttpResponse response) {
        try {
            return response.getStatusCode().toString();
        } catch (IOException e) {
            return "500";
        }
    }

    @Override
    protected String getTargetEntity(HttpRequest clientRequest) {
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

}
