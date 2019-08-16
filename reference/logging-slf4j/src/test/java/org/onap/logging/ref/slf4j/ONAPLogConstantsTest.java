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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * Tests for {@link ONAPLogConstants}.
 */
public class ONAPLogConstantsTest {

    @Test
    public void testConstructors() throws Exception {
        assertInaccessibleConstructor(ONAPLogConstants.class);
        assertInaccessibleConstructor(ONAPLogConstants.MDCs.class);
        assertInaccessibleConstructor(ONAPLogConstants.Markers.class);
        assertInaccessibleConstructor(ONAPLogConstants.Headers.class);
    }

    @Test
    public void testConstructorUnsupported() throws Exception {
        try {
            Constructor<?> c = ONAPLogConstants.class.getDeclaredConstructors()[0];
            c.setAccessible(true);
            c.newInstance();
            Assert.fail("Should fail for hidden constructor.");
        }
        catch (final InvocationTargetException e) {
            assertThat(e.getCause(), instanceOf(UnsupportedOperationException.class));
        }
    }

    @Test
    public void testHeaders() {
        assertThat(ONAPLogConstants.Headers.REQUEST_ID, is("X-ONAP-RequestID"));
        assertThat(ONAPLogConstants.Headers.INVOCATION_ID, is("X-ONAP-InvocationID"));
        assertThat(ONAPLogConstants.Headers.PARTNER_NAME, is("X-ONAP-PartnerName"));
    }

    @Test
    public void testMarkers() {
        assertThat(ONAPLogConstants.Markers.ENTRY.toString(), is("ENTRY"));
        assertThat(ONAPLogConstants.Markers.EXIT.toString(), is("EXIT"));
        assertThat(ONAPLogConstants.Markers.INVOKE.toString(), is("INVOKE"));
        assertThat(ONAPLogConstants.Markers.INVOKE_ASYNCHRONOUS.toString(), is("INVOKE [ ASYNCHRONOUS ]"));
        assertThat(ONAPLogConstants.Markers.INVOKE_SYNCHRONOUS.toString(), is("INVOKE [ SYNCHRONOUS ]"));
    }

    @Test
    public void testInvocationMode() {
        assertThat(ONAPLogConstants.InvocationMode.SYNCHRONOUS.getMarker(),
                is(ONAPLogConstants.Markers.INVOKE_SYNCHRONOUS));
        assertThat(ONAPLogConstants.InvocationMode.ASYNCHRONOUS.getMarker(),
                is(ONAPLogConstants.Markers.INVOKE_ASYNCHRONOUS));
    }

    @Test
    public void testInvocationModeToString() {
        assertThat(ONAPLogConstants.InvocationMode.SYNCHRONOUS.toString(),
                is("SYNCHRONOUS"));
    }

    @Test
    public void testResponseStatus() {
        assertThat(ONAPLogConstants.ResponseStatus.COMPLETE.toString(), is("COMPLETE"));
        assertThat(ONAPLogConstants.ResponseStatus.ERROR.toString(), is("ERROR"));
    }

    @Test
    public void testMDCs() {

        assertThat(ONAPLogConstants.MDCs.CLIENT_IP_ADDRESS.toString(), is("ClientIPAddress"));
        assertThat(ONAPLogConstants.MDCs.SERVER_FQDN.toString(), is("ServerFQDN"));

        assertThat(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP.toString(), is("EntryTimestamp"));
        assertThat(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP.toString(), is("InvokeTimestamp"));

        assertThat(ONAPLogConstants.MDCs.REQUEST_ID.toString(), is("RequestID"));
        assertThat(ONAPLogConstants.MDCs.INVOCATION_ID.toString(), is("InvocationID"));
        assertThat(ONAPLogConstants.MDCs.PARTNER_NAME.toString(), is("PartnerName"));
        assertThat(ONAPLogConstants.MDCs.INSTANCE_UUID.toString(), is("InstanceUUID"));
        assertThat(ONAPLogConstants.MDCs.SERVICE_NAME.toString(), is("ServiceName"));
        assertThat(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME.toString(), is("TargetServiceName"));

    }

    static void assertInaccessibleConstructor(final Class<?> c) throws Exception {
        try {
            c.getDeclaredConstructors()[0].newInstance();
            Assert.fail("Should fail for hidden constructor.");
        }
        catch (final IllegalAccessException e) {

        }

        try {
            final Constructor<?> constructor = c.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            constructor.newInstance();
            Assert.fail("Should fail even when invoked.");
        }
        catch (final InvocationTargetException e) {
            assertThat(e.getCause(), instanceOf(UnsupportedOperationException.class));
        }
    }
}
