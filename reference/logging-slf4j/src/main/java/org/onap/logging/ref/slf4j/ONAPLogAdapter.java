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

package org.onap.logging.ref.slf4j;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.Level;

/**
 * Extensible adapter for cheaply meeting ONAP logging obligations using
 * an SLF4J facade.
 *
 * <p>This can be used with any SLF4J-compatible logging provider, with
 * appropriate provider configuration.</p>
 *
 * <p>The basics are that:
 * <ul>
 *     <li>{@link #entering} sets all MDCs.</li>
 *     <li>{@link #exiting} unsets all MDCs *and* logs response information.</li>
 *     <li>{@link #invoke} logs and returns a UUID to passed during invocation,
 *     and optionally sets these for you on your downstream request by way of
 *     an adapter.</li>
 *     <li>Call {@link #getServiceDescriptor()} and its setters to set service-related MDCs.</li>
 *     <li>Call {@link #getResponseDescriptor()} and its setters to set response-related MDCs.</li>
 * </ul>
 * </p>
 *
 * <p>Minimal usage is:
 * <ol>
 *     <li>#entering(RequestAdapter)</li>
 *     <li>#invoke, #invoke, ...</li>
 *     <li>#getResponse + setters (or #setResponse)</li>
 *     <li>#exiting</li>
 * </ol>
 * </p>
 *
 * <p> ... if you're happy for service information to be automatically derived as follows:
 * <ul>
 *     <li><tt>ServiceName</tt> - from <tt>HttpServletRequest#getRequestURI()</tt></li>
 *     <li><tt>InstanceUUID</tt> - classloader-scope UUID.</li>
 * </ul>
 * </p>
 *
 * <p>... and if those defaults don't suit, then you can override using properties on
 * {@link #getServiceDescriptor()}, or by injecting your own adapter using
 * {@link #setServiceDescriptor(ServiceDescriptor)}, or by overriding
 * a <tt>protected</tt> methods like{@link #setEnteringMDCs}.</p>
 *
 * <p>For everything else:
 * <ul>
 *     <li>The underlying SLF4J {@link Logger} can be retrieved using {@link #unwrap}.
 *     Use this or create your own using the usual SLF4J factor.</li>
 *     <li>Set whatever MDCs you like.</li>
 *     <li>Log whatever else you like.</li>
 * </ul>
 * </p>
 */
public class ONAPLogAdapter {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Constants.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** String constant for messages <tt>ENTERING</tt>, <tt>EXITING</tt>, etc. */
    private static final String EMPTY_MESSAGE = "";

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Fields.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Automatic UUID, overrideable per adapter or per invocation. */
    private static UUID sInstanceUUID = UUID.randomUUID();

    /** Logger delegate. */
    private Logger mLogger;

    /** Overrideable descriptor for the service doing the logging. */
    private ServiceDescriptor mServiceDescriptor = new ServiceDescriptor();

    /** Overrideable descriptor for the response returned by the service doing the logging. */
    private ResponseDescriptor mResponseDescriptor = new ResponseDescriptor();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Constructors.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Construct adapter.
     *
     * @param logger non-null logger.
     */
    public ONAPLogAdapter(final Logger logger) {
        this.mLogger = checkNotNull(logger);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Public methods.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get logger.
     *
     * @return unwrapped logger.
     */
    public Logger unwrap() {
        return this.mLogger;
    }

    /**
     * Report <tt>ENTERING</tt> marker.
     *
     * @param request non-null incoming request (wrapper).
     * @return this.
     */
    public ONAPLogAdapter entering(final RequestAdapter request) {

        checkNotNull(request);

        // Default the service name.

        this.setEnteringMDCs(request);
        this.mLogger.info(ONAPLogConstants.Markers.ENTRY, EMPTY_MESSAGE);

        return this;
    }

    /**
     * Report <tt>ENTERING</tt> marker.
     *
     * @param request non-null incoming request.
     * @return this.
     */
    public ONAPLogAdapter entering(final HttpServletRequest request) {
        return this.entering(new HttpServletRequestAdapter(checkNotNull(request)));
    }

    /**
     * Report <tt>EXITING</tt> marker.
     *
     * @return this.
     */
    public ONAPLogAdapter exiting() {
        try {
            this.mResponseDescriptor.setMDCs();
            this.mLogger.info(ONAPLogConstants.Markers.EXIT, EMPTY_MESSAGE);
        }
        finally {
            MDC.clear();
        }
        return this;
    }

    /**
     * Report pending invocation with <tt>INVOKE</tt> marker.
     *
     * <p>If you call this variant, then YOU are assuming responsibility for
     * setting the requisite ONAP headers.</p>
     *
     * @param sync whether synchronous.
     * @return invocation ID to be passed with invocation.
     */
    public UUID invoke(final ONAPLogConstants.InvocationMode sync) {

        final UUID invocationID = UUID.randomUUID();

        // Derive SYNC/ASYNC marker.

        final Marker marker = (sync == null) ? ONAPLogConstants.Markers.INVOKE : sync.getMarker();

        // Log INVOKE*, with the invocationID as the message body.
        // (We didn't really want this kind of behavior in the standard,
        // but is it worse than new, single-message MDC?)

        this.mLogger.info(marker, "{}", invocationID);
        return invocationID;
    }

    /**
     * Report pending invocation with <tt>INVOKE</tt> marker,
     * setting standard ONAP logging headers automatically.
     *
     * @param builder request builder, for setting headers.
     * @param sync whether synchronous, nullable.
     * @return invocation ID to be passed with invocation.
     */
    public UUID invoke(final RequestBuilder builder,
                       final ONAPLogConstants.InvocationMode sync) {

        // Sync can be defaulted. Builder cannot.

        checkNotNull(builder);

        // Log INVOKE, and retain invocation ID for header + return.

        final UUID invocationID = this.invoke(sync);

        // Set standard HTTP headers on (southbound request) builder.

        builder.setHeader(ONAPLogConstants.Headers.REQUEST_ID,
                defaultToEmpty(MDC.get(ONAPLogConstants.MDCs.REQUEST_ID)));
        builder.setHeader(ONAPLogConstants.Headers.INVOCATION_ID,
                defaultToEmpty(invocationID));
        builder.setHeader(ONAPLogConstants.Headers.PARTNER_NAME,
                defaultToEmpty(MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME)));

        return invocationID;
    }

    /**
     * Report vanilla <tt>INVOKE</tt> marker.
     *
     * @param builder builder for downstream requests, if you want the
     *                standard ONAP headers to be added automatically.
     * @return invocation ID to be passed with invocation.
     */
    public UUID invoke(final RequestBuilder builder) {
        return this.invoke(builder, (ONAPLogConstants.InvocationMode)null);
    }

    /**
     * Get descriptor, for overriding service details.
     * @return non-null descriptor.
     */
    public ServiceDescriptor getServiceDescriptor() {
        return checkNotNull(this.mServiceDescriptor);
    }

    /**
     * Override {@link ServiceDescriptor}.
     * @param d non-null override.
     * @return this.
     */
    public ONAPLogAdapter setServiceDescriptor(final ServiceDescriptor d) {
        this.mServiceDescriptor = checkNotNull(d);
        return this;
    }

    /**
     * Get descriptor, for setting response details.
     * @return non-null descriptor.
     */
    public ResponseDescriptor getResponseDescriptor() {
        return checkNotNull(this.mResponseDescriptor);
    }

    /**
     * Override {@link ResponseDescriptor}.
     * @param d non-null override.
     * @return this.
     */
    public ONAPLogAdapter setResponseDescriptor(final ResponseDescriptor d) {
        this.mResponseDescriptor = checkNotNull(d);
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Protected methods.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Set MDCs that persist for the duration of an invocation.
     *
     * <p>It would be better to roll this into {@link #entering}, like
     * with {@link #exiting}. Then it would be easier to do, but it
     * would mean more work. </p>
     *
     * @param request incoming HTTP request.
     * @return this.
     */
    protected ONAPLogAdapter setEnteringMDCs(final RequestAdapter<?> request) {

        // Extract MDC values from standard HTTP headers.

        final String requestID = defaultToUUID(request.getHeader(ONAPLogConstants.Headers.REQUEST_ID));
        final String invocationID = defaultToUUID(request.getHeader(ONAPLogConstants.Headers.INVOCATION_ID));
        final String partnerName = defaultToEmpty(request.getHeader(ONAPLogConstants.Headers.PARTNER_NAME));

        // Set standard MDCs. Override this entire method if you want to set
        // others, OR set them BEFORE or AFTER the invocation of #entering,
        // depending on where you need them to appear, OR extend the
        // ServiceDescriptor to add them.

        MDC.put(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP,
                ZonedDateTime.now(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT));
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, requestID);
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, invocationID);
        MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME, partnerName);
        MDC.put(ONAPLogConstants.MDCs.CLIENT_IP_ADDRESS, defaultToEmpty(request.getClientAddress()));
        MDC.put(ONAPLogConstants.MDCs.SERVER_FQDN, defaultToEmpty(request.getServerAddress()));

        // Delegate to the service adapter, for service-related DMCs.

        this.mServiceDescriptor.setMDCs();

        // Default the service name to the requestURI, in the event that
        // no value has been provided.

        if (MDC.get(ONAPLogConstants.MDCs.SERVICE_NAME) == null ||
                MDC.get(ONAPLogConstants.MDCs.SERVICE_NAME).equalsIgnoreCase(EMPTY_MESSAGE)) {
            MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, request.getRequestURI());
        }

        return this;
    }

    /**
     * Dependency-free nullcheck.
     *
     * @param in to be checked.
     * @param <T> argument (and return) type.
     * @return input arg.
     */
    protected static <T> T checkNotNull(final T in) {
        if (in == null) {
            throw new NullPointerException();
        }
        return in;
    }

    /**
     * Dependency-free string default.
     *
     * @param in to be filtered.
     * @return input string or null.
     */
    protected static String defaultToEmpty(final Object in) {
        if (in == null) {
            return "";
        }
        return in.toString();
    }

    /**
     * Dependency-free string default.
     *
     * @param in to be filtered.
     * @return input string or null.
     */
    protected static String defaultToUUID(final String in) {
        if (in == null) {
            return UUID.randomUUID().toString();
        }
        return in;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Inner classes.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Extensible descriptor for reporting service details.
     *
     * <p>In most cases extension isn't required. </p>
     */
    public static class ServiceDescriptor {

        /** <tt>ServiceName</tt>. */
        protected String mName;

        /** <tt>InstanceUUID</tt>. */
        protected String mUUID = sInstanceUUID.toString();

        /**
         * Set name.
         * @param name <tt>ServiceName</tt>.
         * @return this.
         */
        public ServiceDescriptor setServiceName(final String name) {
            this.mName = name;
            return this;
        }

        /**
         * Set name.
         * @param uuid <tt>InstanceUUID</tt>.
         * @return this.
         */
        public ServiceDescriptor setServiceUUID(final String uuid) {
            this.mUUID = uuid;
            return this;
        }

        /**
         * Set MDCs. Once set they remain set until everything is cleared.
         */
        protected void setMDCs() {
            MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, defaultToEmpty(this.mName));
            MDC.put(ONAPLogConstants.MDCs.INSTANCE_UUID, defaultToEmpty(this.mUUID));
        }
    }

    /**
     * Response is different in that response MDCs are normally only
     * reported once, for a single log message. (But there's no method
     * for clearing them, because this is only expected to be called
     * during <tt>#exiting</tt>.)
     */
    public static class ResponseDescriptor {

        /** Response errorcode. */
        protected String mCode;

        /** Response description. */
        protected String mDescription;

        /** Response severity. */
        protected Level mSeverity;

        /** Response status, of {<tt>COMPLETED</tt>, <tt>ERROR</tt>}. */
        protected ONAPLogConstants.ResponseStatus mStatus;

        /**
         * Setter.
         *
         * @param code response (error) code.
         * @return this.
         */
        public ResponseDescriptor setResponseCode(final String code) {
            this.mCode = code;
            return this;
        }

        /**
         * Setter.
         *
         * @param description response description.
         * @return this.
         */
        public ResponseDescriptor setResponseDescription(final String description) {
            this.mDescription = description;
            return this;
        }

        /**
         * Setter.
         *
         * @param severity response outcome severity.
         * @return this.
         */
        public ResponseDescriptor setResponseSeverity(final Level severity) {
            this.mSeverity = severity;
            return this;
        }

        /**
         * Setter.
         *
         * @param status response overall status.
         * @return this.
         */
        public ResponseDescriptor setResponseStatus(final ONAPLogConstants.ResponseStatus status) {
            this.mStatus = status;
            return this;
        }

        /**
         * Overrideable method to set MDCs based on property values.
         */
        protected void setMDCs() {
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, defaultToEmpty(this.mCode));
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION, defaultToEmpty(this.mDescription));
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_SEVERITY, defaultToEmpty(this.mSeverity));
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, defaultToEmpty(this.mStatus));
        }
    }

    /**
     * Adapter for reading information from an incoming HTTP request.
     *
     * <p>Incoming is generally easy, because in most cases you'll be able to
     * get your hands on the <tt>HttpServletRequest</tt>.</p>
     *
     * <p>Perhaps should be generalized to refer to constants instead of
     * requiring the implementation of specific methods.</p>
     *
     * @param <T> type, for chaining.
     */
    public interface RequestAdapter<T extends RequestAdapter> {

        /**
         * Get header by name.
         * @param name header name.
         * @return header value, or null.
         */
        String getHeader(String name);

        /**
         * Get client address.
         * @return address, if available.
         */
        String getClientAddress();

        /**
         * Get server address.
         * @return address, if available.
         */
        String getServerAddress();

        /**
         * Get default service name, from service URI.
         * @return service name default.
         */
        String getRequestURI();
    }

    /**
     * Default {@link RequestBuilder} impl for {@link HttpServletRequest}, which
     * will should available for most incoming REST requests.
     */
    public static class HttpServletRequestAdapter implements RequestAdapter<HttpServletRequestAdapter> {

        /** Wrapped HTTP request. */
        private final HttpServletRequest mRequest;

        /**
         * Construct adapter for HTTP request.
         * @param request to be wrapped;
         */
        public HttpServletRequestAdapter(final HttpServletRequest request) {
            this.mRequest = checkNotNull(request);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getHeader(final String name) {
            return this.mRequest.getHeader(name);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getClientAddress() {
            return this.mRequest.getRemoteAddr();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getServerAddress() {
            return this.mRequest.getServerName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getRequestURI() {
            return this.mRequest.getRequestURI();
        }
    }

    /**
     * Header builder, which (unlike {@link RequestAdapter} will tend to
     * vary a lot from caller to caller, since they each get to choose their
     * own REST (or HTTP, or whatever) client APIs.
     *
     * <p>No default implementation, because there's no HTTP client that's
     * sufficiently ubiquitous to warrant incurring a mandatory dependency.</p>
     *
     * @param <T> type, for chaining.
     */
    public interface RequestBuilder<T extends RequestBuilder> {

        /**
         * Set HTTP header.
         * @param name header name.
         * @param value header value.
         * @return this.
         */
        T setHeader(String name, String value);
    }
}
