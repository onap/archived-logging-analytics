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

package org.onap.logging.ref.slf4j.analysis;

import java.util.Map;

import org.slf4j.event.Level;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class LogEntryTest {

    @Test
    public void testLogEntry() {

        final String eg = "2018-05-07T16:45:53.056Z\tpool-1-thread-1\tINFO"
            + "\torg.onap.logging.ref.slf4j.component.gamma.ComponentGamma\tInstanceUUID=fa8dd337-6991-4535-a069-ca552466d972,"
            + " RequestID=46161759-1b92-40a4-a408-800e0d62dd9e, ServiceName=service.alpha, EntryTimestamp=2018-05-08T02:45:53.056,"
            + " InvocationID=aac8fec9-498c-42a2-936b-38f5c0f5ca82, PartnerName=service.beta, ClientIPAddress=127.0.0.1,"
            + " ServerFQDN=localhost\t\t\tENTRY\t\n";

        final LogEntry parsed = new LogEntry(eg);
        assertThat(parsed.getTimestamp(), notNullValue());
        assertThat(parsed.getThread(), is("pool-1-thread-1"));
        assertThat(parsed.getLevel(), is(Level.INFO));
        assertThat(parsed.getLogger(), is("org.onap.logging.ref.slf4j.component.gamma.ComponentGamma"));
        assertThat(parsed.getMDCs().get("ServiceName"), is("service.alpha"));
        assertThat(parsed.getMDCs().get("PartnerName"), is("service.beta"));
        assertThat(parsed.getMessage(), is(""));
        assertThat(parsed.getMarkers(), is("ENTRY"));
        assertThat(parsed.getException(), is(""));

    }

    @Test
    public void testParseMDCsEmpty() {
        final Map<String, String> map = LogEntry.parseMDCs("");
        assertThat(map.size(), is(0));
    }

    @Test
    public void testParseMDCs() {
        final Map<String, String> map = LogEntry.parseMDCs("A=B, C=D , D = F ");
        assertThat(map.get("A"), is("B"));
        assertThat(map.get("C"), is("D"));
        assertThat(map.get("D"), is("F"));
        assertThat(map.size(), is(3));
    }
}
