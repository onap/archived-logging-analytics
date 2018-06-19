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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.number.OrderingComparison.greaterThan;

/**
 * Smoketest output, though the embedded configuration isn't necessarily
 * canonical.
 *
 * <p>There are more comprehensive tests in the <tt>logging-slf4j-demo</tt>
 * project.</p>
 */
public class ONAPLogAdapterOutputTest {

    /** Temporary directory into which logfiles are written. */
    private static File sDir;

    @BeforeSuite
    public static void setUp() throws Exception {
        sDir = Files.createTempDirectory(ONAPLogAdapterOutputTest.class.getName()).toFile();
        System.getProperties().setProperty("TESTNG_SLF4J_OUTPUT_DIRECTORY", sDir.getAbsolutePath());
        LoggerFactory.getLogger(ONAPLogAdapterOutputTest.class).info("Starting.");
    }

    @AfterSuite
    public static void tearDown() throws Exception {
        LoggerFactory.getLogger(ONAPLogAdapterOutputTest.class).info("Ending.");
        Thread.sleep(1000L);
        if (sDir != null) {
            System.err.println("Should be deleting [" + sDir.getAbsolutePath() + "]...");
        }
    }

    @Test
    public void testOutput() throws Exception {

        assertThat(sDir, notNullValue());
        assertThat(sDir.isDirectory(), is(true));

        final String uuid = UUID.randomUUID().toString();
        final String errorcode = UUID.randomUUID().toString();
        final Logger logger = LoggerFactory.getLogger(ONAPLogAdapterOutputTest.class);

        try {
            MDC.put("uuid", uuid);
            final ONAPLogAdapter adapter = new ONAPLogAdapter(logger);
            final ONAPLogAdapter.HttpServletRequestAdapter http
                    = new ONAPLogAdapter.HttpServletRequestAdapter(new MockHttpServletRequest());
            adapter.entering(http);
            adapter.unwrap().warn("a_warning");
            try {
                throw new Exception("errorcode=" + errorcode);
            }
            catch (final Exception e) {
                adapter.unwrap().error("an_error", e);
            }

            Thread.sleep(1000L);
        }
        finally {
            MDC.clear();
        }

        final List<String> lines = new ArrayList<>();
        for (final File f : sDir.listFiles()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(uuid)) {
                        lines.add(line);
                    }
                }
            }
        }

        assertThat(lines.size(), is(3));

        assertThat(lines.get(0), containsString("ENTRY"));
        final String [] line0 = lines.get(0).split("\t", -1);
        assertThat(line0.length, is(9));
        final long sanity = DatatypeConverter.parseDateTime(line0[0]).getTimeInMillis();
        assertThat(Math.abs(System.currentTimeMillis() - sanity), lessThan(5000L));
        assertThat(line0[0], endsWith("Z"));
        assertThat(line0[1].trim().length(), greaterThan(1));
        assertThat(line0[2], is("INFO"));
        assertThat(line0[3], is(this.getClass().getName()));
        assertThat(line0[4], containsString("uuid=" + uuid));
        assertThat(line0[5], is(""));
        assertThat(line0[6], is(""));
        assertThat(line0[7], is("ENTRY"));
        System.err.println(lines.get(0));

        assertThat(lines.get(1), not(containsString("ENTRY")));
        assertThat(lines.get(1), containsString("a_warning"));
        final String [] line1 = lines.get(1).split("\t", -1);
        assertThat(line1.length, is(9));
        DatatypeConverter.parseDateTime(line1[0]);
        assertThat(line1[0], endsWith("Z"));
        assertThat(line1[1].trim().length(), greaterThan(1));
        assertThat(line1[2], is("WARN"));
        assertThat(line1[3], is(this.getClass().getName()));
        assertThat(line1[4], containsString("uuid=" + uuid));
        assertThat(line1[5], is("a_warning"));
        assertThat(line1[6], is(""));
        assertThat(line1[7], is(""));
        System.err.println(lines.get(1));

        assertThat(lines.get(2), not(containsString("ENTRY")));
        assertThat(lines.get(2), containsString("an_error"));
        final String [] line2 = lines.get(2).split("\t", -1);
        assertThat(line2.length, is(9));
        DatatypeConverter.parseDateTime(line2[0]);
        assertThat(line2[0], endsWith("Z"));
        assertThat(line2[1].trim().length(), greaterThan(1));
        assertThat(line2[2], is("ERROR"));
        assertThat(line2[3], is(this.getClass().getName()));
        assertThat(line2[4], containsString("uuid=" + uuid));
        assertThat(line2[5], is("an_error"));
        assertThat(line2[6], containsString("errorcode=" + errorcode));
        assertThat(line2[7], is(""));
        System.err.println(lines.get(2));
    }
}
