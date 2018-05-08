/**
 * ============LICENSE_START=======================================================
 * org.onap.logging
 * ================================================================================
 * Copyright © 2018 Amdocs
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.logging.ref.slf4j.demo.component;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.onap.logging.ref.slf4j.common.ONAPLogAdapter;
import org.onap.logging.ref.slf4j.common.ONAPLogConstants;
import org.onap.logging.ref.slf4j.demo.bean.Request;
import org.onap.logging.ref.slf4j.demo.bean.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Base class for <tt>Alpha</tt>, <tt>Beta</tt> and <tt>Gamma</tt>
 * and <tt>Delta</tt> controllers, implementing all the actual logic.
 *
 * <p>(The subclasses provide nothing but identifiers to allow them
 * to be distinguished from one another, for the purposes of addressing
 * requests and generating the call graph from their logger output.)</p>
 */
@RestController
public abstract class AbstractComponent {

    /**
     * Test switch, routing invocations between components in-process,
     * rather than via REST over HTTP.
     */
    private static boolean sInProcess;

    /**
     * Get service identifier, used to derive {@link #getServiceName()},
     * <tt>PartnerName</tt>, etc.
     * @return <tt>alpha</tt>, <tt>beta</tt>, <tt>gamma</tt>.
     */
    protected abstract String getId();

    /**
     * Get component UUID.
     * @return globally unique ID string.
     */
    protected abstract String getInstanceUUID();

    /**
     * Execute REST request.
     * @param request request data.
     * @param http HTTP request.
     * @return response data.
     * @throws UnirestException REST error.
     */
    @RequestMapping(value = "/invoke",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Response execute(final Request request,
                            final HttpServletRequest http) throws UnirestException {

        final ONAPLogAdapter adapter = new ONAPLogAdapter(this.getLogger());

        try {

            adapter.entering(new ONAPLogAdapter.HttpServletRequestAdapter(http));

            final Response response = new Response();
            response.setService(request.getService());
            final String code = StringUtils.defaultString(request.getCode(), "OK").toUpperCase();
            response.setCode(this.getId() + "." + code);
            response.setSeverity(StringUtils.defaultString(request.getSeverity(), "INFO"));

            for (final Request target : request.getRequests()) {
                final Response targetResponse = this.executeDelegate(target, http, adapter);
                response.getResponses().add(targetResponse);
            }

            return response;
        }
        finally {
            adapter.exiting();
        }
    }

    /**
     * Set in-process mode, for unit testing.
     */
    static void setInProcess() {
        sInProcess = true;
    }

    /**
     * Execute request.
     * @param request to be executed.
     * @param http incoming HTTP request.
     * @param logger logging adapter.
     * @return response
     */
    private Response executeDelegate(final Request request,
                                     final HttpServletRequest http,
                                     final ONAPLogAdapter logger) {


        notNull(request);
        notNull(http);

        // Downstream call.

        try {

            if (sInProcess) {
                return this.executeInProcess(request, logger);
            }

            return this.executeREST(request, http, logger);
        }
        catch (final UnirestException | ReflectiveOperationException e) {
            logger.unwrap().error("Execute error", e);
            final Response response = new Response();
            response.setCode((this.getServiceName() + ".INVOKE_ERROR").toUpperCase(Locale.getDefault()));
            response.setSeverity("ERROR");
            return response;
        }
    }

    /**
     * Execute invocation over REST.
     * @param request mock request to be executed.
     * @param http HTTP request, used (only) to address the outgoing request.
     * @param logger logger adapter.
     * @return invocation response.
     * @throws UnirestException REST error.
     */
    private Response executeREST(final Request request,
                                 final HttpServletRequest http,
                                 final ONAPLogAdapter logger) throws UnirestException {
        // Otherwise via REST.

        logger.unwrap().info("Sending:\n{}", request);
        final StringBuilder url = new StringBuilder();
        url.append(http.getProtocol()).append("://");
        url.append(http.getServerName()).append(':');
        url.append(http.getServerPort()).append("/services/").append(request.getService());

        final UUID invocationID = logger.invoke(ONAPLogConstants.InvocationMode.SYNCHRONOUS);
        final HttpResponse<JsonNode> response =
                Unirest.post(url.toString())
                        .header(ONAPLogConstants.Headers.REQUEST_ID, MDC.get(ONAPLogConstants.MDCs.REQUEST_ID))
                        .header(ONAPLogConstants.Headers.INVOCATION_ID, invocationID.toString())
                        .header(ONAPLogConstants.Headers.PARTNER_NAME, this.getServiceName())
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .body(request)
                        .asJson();

        // Parse response.

        final JSONObject responseJSON = response.getBody().getObject();
        logger.unwrap().info("Received:\n{}", responseJSON);
        return Response.fromJSON(responseJSON);
    }

    /**
     * Execute request in-process.
     * @param request mock request to be executed.
     * @param logger logger adapter.
     * @return invocation response.
     * @throws ReflectiveOperationException error loading target class.
     * @throws UnirestException REST error.
     */
    private Response executeInProcess(final Request request,
                                      final ONAPLogAdapter logger) throws ReflectiveOperationException, UnirestException {

        logger.unwrap().info("Executing in-process:\n{}", request);

        // Derive the name of the delegate class.

        final String delegateClass
                = AbstractComponent.class.getPackage().getName() + "." + request.getService()
                + ".Component" + request.getService().substring(0, 1).toUpperCase()
                + request.getService().substring(1);
        logger.unwrap().info("Invoking in-process [{}].", delegateClass);
        final AbstractComponent component = (AbstractComponent)Class.forName(delegateClass).newInstance();

        // Using Spring mock since we're not *actually* going over HTTP.

        final MockHttpServletRequest mock = new MockHttpServletRequest();

        // Generate INVOCATION_ID, and set MDCs aside for safekeeping.
        // (This is because when mocking, everything happens in the same thread.)

        final UUID invocationID = logger.invoke(ONAPLogConstants.InvocationMode.SYNCHRONOUS);
        final String requestID = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
        final Map<String, String> safekeeping = MDC.getCopyOfContextMap();

        // Set headers.

        mock.addHeader(ONAPLogConstants.Headers.REQUEST_ID, StringUtils.defaultString(requestID));
        mock.addHeader(ONAPLogConstants.Headers.INVOCATION_ID, invocationID.toString());
        mock.addHeader(ONAPLogConstants.Headers.PARTNER_NAME, this.getServiceName());

        try {

            MDC.clear();

            // Execute.

            return component.execute(request, mock);
        }
        finally {

            // Restore MDCs.

            safekeeping.forEach((k, v) -> MDC.put(k, v));
        }
    }

    /**
     * Ensure non-nullness.
     * @param in to be checked.
     * @param <T> type.
     * @return input value, not null.
     */
    private static <T> T notNull(final T in) {
        if (in == null) {
            throw new AssertionError("");
        }
        return in;
    }

    /**
     * Get service name, with default.
     * @return service name, suitable for logging as MDC.
     */
    private String getServiceName() {
        return "service." + StringUtils.defaultString(this.getId(), "unnamed");
    }

    /**
     * Get logger instance.
     * @return logger.
     */
    private Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }
}
