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

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Constants for standard ONAP headers, MDCs, etc.
 *
 * <p>See <tt>package-info.java</tt>.</p>
 */
public final class ONAPLogConstants {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Constructors.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Hide and forbid construction.
     */
    private ONAPLogConstants() {
        throw new UnsupportedOperationException();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Inner classes.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Marker constants.
     */
    public static final class Markers {

        /** Marker reporting invocation. */
        public static final Marker INVOKE = MarkerFactory.getMarker("INVOKE");
        
        /** Marker reporting invocation. */
        public static final Marker INVOKE_RETURN = MarkerFactory.getMarker("INVOKE_RETURN");

        /** Marker reporting synchronous invocation. */
        public static final Marker INVOKE_SYNCHRONOUS = build("INVOKE", "SYNCHRONOUS");

        /** Marker reporting asynchronous invocation. */
        public static final Marker INVOKE_ASYNCHRONOUS = build("INVOKE", "ASYNCHRONOUS");

        /** Marker reporting entry into a component. */
        public static final Marker ENTRY = MarkerFactory.getMarker("ENTRY");

        /** Marker reporting exit from a component. */
        public static final Marker EXIT = MarkerFactory.getMarker("EXIT");

        /**
         * Build nested, detached marker.
         * @param m1 top token.
         * @param m2 sub-token.
         * @return detached Marker.
         */
        private static Marker build(final String m1, final String m2) {
            final Marker marker = MarkerFactory.getDetachedMarker(m1);
            marker.add(MarkerFactory.getDetachedMarker(m2));
            return marker;
        }

        /**
         * Hide and forbid construction.
         */
        private Markers() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * MDC name constants.
     */
    public static final class MDCs {

        // Tracing. ////////////////////////////////////////////////////////////

        /** MDC correlating messages for an invocation. */
        public static final String INVOCATION_ID = "InvocationID";

        /** MDC correlating messages for a logical transaction. */
        public static final String REQUEST_ID = "RequestID";

        /** MDC recording calling service. */
        public static final String PARTNER_NAME = "PartnerName";

        /** MDC recording current service. */
        public static final String SERVICE_NAME = "ServiceName";

        /** MDC recording target service. */
        public static final String TARGET_SERVICE_NAME = "TargetServiceName";
        
        /** MDC recording target entity. */
        public static final String TARGET_ENTITY = "TargetEntity";

        /** MDC recording current service instance. */
        public static final String INSTANCE_UUID = "InstanceUUID";

        // Network. ////////////////////////////////////////////////////////////

        /** MDC recording caller address. */
        public static final String CLIENT_IP_ADDRESS = "ClientIPAddress";

        /** MDC recording server address. */
        public static final String SERVER_FQDN = "ServerFQDN";

        /**
         * MDC recording timestamp at the start of the current request,
         * with the same scope as {@link #REQUEST_ID}.
         *
         * <p>Open issues:
         * <ul>
         *     <ul>Easily confused with {@link #INVOKE_TIMESTAMP}.</ul>
         *     <ul>No mechanism for propagation between components, e.g. via HTTP headers.</ul>
         *     <ul>Whatever mechanism we define, it's going to be costly.</ul>
         * </ul>
         * </p>
         * */
        public static final String ENTRY_TIMESTAMP = "EntryTimestamp";

        /** MDC recording timestamp at the start of the current invocation. */
        public static final String INVOKE_TIMESTAMP = "InvokeTimestamp";

        // Outcomes. ///////////////////////////////////////////////////////////

        /** MDC reporting outcome code. */
        public static final String RESPONSE_CODE = "ResponseCode";

        /** MDC reporting outcome description. */
        public static final String RESPONSE_DESCRIPTION = "ResponseDescription";

        /** MDC reporting outcome error level. */
        public static final String RESPONSE_SEVERITY = "Severity";

        /** MDC reporting outcome error level. */
        public static final String RESPONSE_STATUS_CODE = "StatusCode";

        // Unsorted. ///////////////////////////////////////////////////////////

        /**
         * Hide and forbid construction.
         */
        private MDCs() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Header name constants.
     */
    public static final class Headers {

        /** HTTP <tt>X-ONAP-RequestID</tt> header. */
        public static final String REQUEST_ID = "X-ONAP-RequestID";

        /** HTTP <tt>X-ONAP-InvocationID</tt> header. */
        public static final String INVOCATION_ID = "X-ONAP-InvocationID";

        /** HTTP <tt>X-ONAP-PartnerName</tt> header. */
        public static final String PARTNER_NAME = "X-ONAP-PartnerName";

        /**
         * Hide and forbid construction.
         */
        private Headers() {
            throw new UnsupportedOperationException();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Enums.
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Response success or not, for setting <tt>StatusCode</tt>.
     */
    public enum ResponseStatus {

        /** Success. */
        COMPLETED,

        /** Not. */
        ERROR,
        
        /** In Progress. */
        INPROGRESS
    }

    /**
     * Synchronous or asynchronous execution, for setting invocation marker.
     */
    public enum InvocationMode {

        /** Synchronous, blocking. */
        SYNCHRONOUS("SYNCHRONOUS", Markers.INVOKE_SYNCHRONOUS),

        /** Asynchronous, non-blocking. */
        ASYNCHRONOUS("ASYNCHRONOUS", Markers.INVOKE_ASYNCHRONOUS);

        /** Enum value. */
        private String mString;

        /** Corresponding marker. */
        private Marker mMarker;

        /**
         * Construct enum.
         *
         * @param s enum value.
         * @param m corresponding Marker.
         */
        InvocationMode(final String s, final Marker m) {
            this.mString = s;
            this.mMarker = m;
        }

        /**
         * Get Marker for enum.
         *
         * @return Marker.
         */
        public Marker getMarker() {
            return this.mMarker;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return this.mString;
        }
    }

}
