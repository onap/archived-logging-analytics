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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.hamcrest.beans.HasProperty.hasProperty;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.hamcrest.collection.IsArray.array;
import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;
import static org.hamcrest.collection.IsArrayContainingInAnyOrder.arrayContainingInAnyOrder;
import static org.hamcrest.collection.IsIn.isIn;
import static org.hamcrest.collection.IsIn.isOneOf;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasValue;
import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.hamcrest.text.StringContainsInOrder.stringContainsInOrder;
import static org.hamcrest.xml.HasXPath.hasXPath;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsSame.sameInstance;

/**
 * Tests for {@link ONAPLogAdapter}.
 */
public class ONAPLogAdapterTest {

    /**
     * Ensure that MDCs are cleared after each testcase.
     */
    @AfterMethod
    public void resetMDCs() {
        MDC.clear();
    }

    /**
     * Test nullcheck.
     */
    @Test
    public void testCheckNotNull() {

        ONAPLogAdapter.checkNotNull("");

        try {
            ONAPLogAdapter.checkNotNull(null);
            Assert.fail("Should throw NullPointerException");
        }
        catch (final NullPointerException e) {

        }
    }

    /**
     * Test defaulting of nulls.
     */
    @Test
    public void testDefaultToEmpty() {
        assertThat(ONAPLogAdapter.defaultToEmpty("123"), is("123"));
        assertThat(ONAPLogAdapter.defaultToEmpty(Integer.valueOf(1984)), is("1984"));
        assertThat(ONAPLogAdapter.defaultToEmpty(null), is(""));
    }

    /**
     * Test defaulting of nulls.
     */
    @Test
    public void testDefaultToUUID() {
        assertThat(ONAPLogAdapter.defaultToUUID("123"), is("123"));
        UUID.fromString(ONAPLogAdapter.defaultToUUID(null));
    }

    /**
     * Test ENTERING.
     */
    @Test
    public void testEntering() {

        final Logger logger = LoggerFactory.getLogger(this.getClass());
        final ONAPLogAdapter adapter = new ONAPLogAdapter(logger);
        final MockHttpServletRequest http = new MockHttpServletRequest();
        http.setRequestURI("uri123");
        http.setServerName("local123");
        http.setRemoteAddr("remote123");
        http.addHeader("X-ONAP-RequestID", "request123");
        http.addHeader("X-InvocationID", "invocation123");
        http.addHeader("X-ONAP-PartnerName", "partner123");

        try {
            adapter.getServiceDescriptor().setServiceName("uri123");
            adapter.entering(http);
            final Map<String, String> mdcs = MDC.getCopyOfContextMap();
            assertThat(mdcs.get("RequestID"), is("request123"));
            assertThat(mdcs.get("InvocationID"), is("invocation123"));
            assertThat(mdcs.get("PartnerName"), is("partner123"));
            assertThat(mdcs.get("ServiceName"), is("uri123"));
            assertThat(mdcs.get("ServerFQDN"), is("local123"));
            assertThat(mdcs.get("ClientIPAddress"), is("remote123"));

            // Timestamp format and value:

            final String invokeTimestampString = mdcs.get("InvokeTimestamp");
            assertThat(invokeTimestampString, notNullValue());
            assertThat(invokeTimestampString, endsWith("Z"));
            final long invokeTimestamp = DatatypeConverter.parseDateTime(invokeTimestampString).getTimeInMillis();
            assertThat(Math.abs(System.currentTimeMillis() - invokeTimestamp), lessThan(5000L));
        }
        finally {
            MDC.clear();
        }
    }
    
    /**
     * Test ENTERING with an EMPTY_STRING serviceName.
     */
    @Test
    public void testEnteringWithEMPTY_STRING_serviceName() {

        final Logger logger = LoggerFactory.getLogger(this.getClass());
        final ONAPLogAdapter adapter = new ONAPLogAdapter(logger);
        final MockHttpServletRequest http = new MockHttpServletRequest();
        http.setRequestURI("uri123");
        http.setServerName("local123");
        http.setRemoteAddr("remote123");
        http.addHeader("X-ONAP-RequestID", "request123");
        http.addHeader("X-InvocationID", "invocation123");
        http.addHeader("X-ONAP-PartnerName", "partner123");

        try {
            // an empty string should kick in setting the actual service name (treated same as null)
            adapter.getServiceDescriptor().setServiceName("");
            adapter.entering(http);
            final Map<String, String> mdcs = MDC.getCopyOfContextMap();
            assertThat(mdcs.get("RequestID"), is("request123"));
            assertThat(mdcs.get("InvocationID"), is("invocation123"));
            assertThat(mdcs.get("PartnerName"), is("partner123"));
            assertThat(mdcs.get("ServiceName"), is("uri123"));
            assertThat(mdcs.get("ServerFQDN"), is("local123"));
            assertThat(mdcs.get("ClientIPAddress"), is("remote123"));

            // Timestamp format and value:

            final String invokeTimestampString = mdcs.get("InvokeTimestamp");
            assertThat(invokeTimestampString, notNullValue());
            assertThat(invokeTimestampString, endsWith("Z"));
            final long invokeTimestamp = DatatypeConverter.parseDateTime(invokeTimestampString).getTimeInMillis();
            assertThat(Math.abs(System.currentTimeMillis() - invokeTimestamp), lessThan(5000L));
        }
        finally {
            MDC.clear();
        }
    }

    @Test
    public void testSetServiceDescriptor() {
        final ONAPLogAdapter.ServiceDescriptor override = new ONAPLogAdapter.ServiceDescriptor();
        final Logger logger = LoggerFactory.getLogger(this.getClass());
        final ONAPLogAdapter adapter = new ONAPLogAdapter(logger);
        final ONAPLogAdapter.ServiceDescriptor before = adapter.getServiceDescriptor();
        adapter.setServiceDescriptor(override);
        final ONAPLogAdapter.ServiceDescriptor after = adapter.getServiceDescriptor();
        assertThat(after, not(sameInstance(before)));
        assertThat(after, is(override));
    }

    @Test
    public void testSetResponseDescriptor() {
        final ONAPLogAdapter.ResponseDescriptor override = new ONAPLogAdapter.ResponseDescriptor();
        final Logger logger = LoggerFactory.getLogger(this.getClass());
        final ONAPLogAdapter adapter = new ONAPLogAdapter(logger);
        final ONAPLogAdapter.ResponseDescriptor before = adapter.getResponseDescriptor();
        adapter.setResponseDescriptor(override);
        final ONAPLogAdapter.ResponseDescriptor after = adapter.getResponseDescriptor();
        assertThat(after, not(sameInstance(before)));
        assertThat(after, is(override));
    }

    @Test
    public void testUnwrap() {
        final Logger logger = LoggerFactory.getLogger(this.getClass());
        final ONAPLogAdapter adapter = new ONAPLogAdapter(logger);
        assertThat(adapter.unwrap(), is(logger));
    }

    /**
     * Test EXITING.
     */
    @Test
    public void testExiting() {

        final Logger logger = LoggerFactory.getLogger(this.getClass());
        final ONAPLogAdapter adapter = new ONAPLogAdapter(logger);

        try {
            MDC.put("somekey", "somevalue");
            assertThat(MDC.get("somekey"), is("somevalue"));
            adapter.exiting();
            assertThat(MDC.get("somekey"), nullValue());
        }
        finally {
            MDC.clear();
        }
    }

    /**
     * Test INVOKE.
     */
    @Test
    public void testInvokeSyncAsyncNull() {

        final Logger logger = LoggerFactory.getLogger(this.getClass());
        final ONAPLogAdapter adapter = new ONAPLogAdapter(logger);

        final UUID syncUUID = adapter.invoke(ONAPLogConstants.InvocationMode.SYNCHRONOUS);
        assertThat(syncUUID, notNullValue());

        final UUID asyncUUID = adapter.invoke(ONAPLogConstants.InvocationMode.SYNCHRONOUS);
        assertThat(asyncUUID, notNullValue());

        final UUID agnosticUUID = adapter.invoke((ONAPLogConstants.InvocationMode)null);
        assertThat(agnosticUUID, notNullValue());

    }

    /**
     * Test INVOKE, with RequestAdapter.
     */
    @Test
    public void testInvokeWithAdapter() throws Exception {

        final Logger logger = LoggerFactory.getLogger(this.getClass());
        final ONAPLogAdapter adapter = new ONAPLogAdapter(logger);

        final Map<String, String> headers = new HashMap<>();
        final ONAPLogAdapter.RequestBuilder builder = new ONAPLogAdapter.RequestBuilder<ONAPLogAdapter.RequestBuilder>() {
            @Override
            public ONAPLogAdapter.RequestBuilder setHeader(final String name, final String value) {
                headers.put(name, value);
                return this;
            }
        };

        try {
            final UUID uuid = adapter.invoke(builder, ONAPLogConstants.InvocationMode.SYNCHRONOUS);
            assertThat(uuid, notNullValue());
            assertThat(headers.get(ONAPLogConstants.Headers.INVOCATION_ID), is(uuid.toString()));
            assertThat(headers.containsKey(ONAPLogConstants.Headers.PARTNER_NAME), is(true));
            assertThat(headers.containsKey(ONAPLogConstants.Headers.REQUEST_ID), is(true));
        }
        finally {
            MDC.clear();
        }
    }

    /**
     * Test INVOKE, with RequestAdapter.
     */
    @Test
    public void testInvokeWithAdapterAndNull() throws Exception {

        final Logger logger = LoggerFactory.getLogger(this.getClass());
        final ONAPLogAdapter adapter = new ONAPLogAdapter(logger);

        final Map<String, String> headers = new HashMap<>();
        final ONAPLogAdapter.RequestBuilder builder = new ONAPLogAdapter.RequestBuilder<ONAPLogAdapter.RequestBuilder>() {
            @Override
            public ONAPLogAdapter.RequestBuilder setHeader(final String name, final String value) {
                headers.put(name, value);
                return this;
            }
        };

        try {
            final UUID uuid = adapter.invoke(builder);
            assertThat(uuid, notNullValue());
            assertThat(headers.get(ONAPLogConstants.Headers.INVOCATION_ID), is(uuid.toString()));
            assertThat(headers.containsKey(ONAPLogConstants.Headers.PARTNER_NAME), is(true));
            assertThat(headers.containsKey(ONAPLogConstants.Headers.REQUEST_ID), is(true));
        }
        finally {
            MDC.clear();
        }
    }

    @Test
    public void testHttpServletRequestAdapter() {

        final UUID uuid = UUID.randomUUID();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("uuid", uuid.toString());
        request.setRequestURI("/ctx0");
        request.setServerName("srv0");

        final ONAPLogAdapter.HttpServletRequestAdapter adapter
                = new ONAPLogAdapter.HttpServletRequestAdapter(request);
        assertThat(adapter.getHeader("uuid"), is(uuid.toString()));
        assertThat(adapter.getRequestURI(), is("/ctx0"));
        assertThat(adapter.getServerAddress(), is("srv0"));
    }

    @Test
    public void testServiceDescriptor() {
        final String uuid = UUID.randomUUID().toString();

        final ONAPLogAdapter.ServiceDescriptor adapter
                = new ONAPLogAdapter.ServiceDescriptor();
        adapter.setServiceUUID(uuid);
        adapter.setServiceName("name0");

        assertThat(MDC.get(ONAPLogConstants.MDCs.SERVICE_NAME), nullValue());
        assertThat(MDC.get(ONAPLogConstants.MDCs.INSTANCE_UUID), nullValue());

        adapter.setMDCs();

        assertThat(MDC.get(ONAPLogConstants.MDCs.SERVICE_NAME), is("name0"));
        assertThat(MDC.get(ONAPLogConstants.MDCs.INSTANCE_UUID), is(uuid));
    }

    @Test
    public void testResponseDescriptor() {
        final String uuid = UUID.randomUUID().toString();

        final ONAPLogAdapter.ResponseDescriptor adapter
                = new ONAPLogAdapter.ResponseDescriptor();
        adapter.setResponseCode("code0");
        adapter.setResponseDescription("desc0");
        adapter.setResponseSeverity(Level.INFO);
        adapter.setResponseStatus(ONAPLogConstants.ResponseStatus.COMPLETE);

        assertThat(MDC.get(ONAPLogConstants.MDCs.RESPONSE_CODE), nullValue());
        assertThat(MDC.get(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION), nullValue());
        assertThat(MDC.get(ONAPLogConstants.MDCs.RESPONSE_SEVERITY), nullValue());
        assertThat(MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE), nullValue());

        adapter.setMDCs();

        assertThat(MDC.get(ONAPLogConstants.MDCs.RESPONSE_CODE), is("code0"));
        assertThat(MDC.get(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION), is("desc0"));
        assertThat(MDC.get(ONAPLogConstants.MDCs.RESPONSE_SEVERITY), is("INFO"));
        assertThat(MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE), is("COMPLETE"));
    }

    /**
     * Exercise the contract, for a caller that's happy to have their
     * service name automatically derived. (This validates nothing
     * and achieves nothing; it's just to provide an example of minimal usage).
     */
    @Test
    public void testContract() {

        // Note no wrapper around HttpServletRequest, which will work for
        // most invocations (since they come via HTTP), but otherwise
        // can implement your own RequestAdapter.

        final Logger logger = LoggerFactory.getLogger(this.getClass());
        final ONAPLogAdapter adapter = new ONAPLogAdapter(logger);
        final MockHttpServletRequest http = new MockHttpServletRequest();

        // Immediately log ENTERING marker, with global MDCs.

        adapter.entering(http);
        try {

            // Generate (and log) an invocationID, then use it to
            // invoke another component.

            final RESTClient client = new RESTClient();             // implements ONAPLogAdapter.RequestBuilder<RESTClient>.
            adapter.invoke(client, ONAPLogConstants.InvocationMode.SYNCHRONOUS);
            final RESTRequest request = null;                       // TODO: build real request.
            final RESTResponse response = client.execute(request);  // TODO: handle real response.

            // Set response details prior to #exiting.
            // (Obviously there'd be errorhandling, etc. IRL).

            adapter.getResponseDescriptor()
                    .setResponseCode((String)null)
                    .setResponseSeverity(Level.INFO)
                    .setResponseStatus(ONAPLogConstants.ResponseStatus.COMPLETE);
        }
        finally {

            // Return, logging EXIT marker, with response MDCs.

            adapter.exiting();
        }
    }

    /**
     * Dummy class, for example code.
     */
    static class RESTClient implements ONAPLogAdapter.RequestBuilder<RESTClient> {

        @Override
        public RESTClient setHeader(final String name, final String value) {
            return null;
        }

        RESTResponse execute(RESTRequest request) {
            return null;
        }
    }

    /**
     * Dummy class, for example code.
     */
    static class RESTRequest {

    }

    /**
     * Dummy class, for example code.
     */
    static class RESTResponse {

    }
}
