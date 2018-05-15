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

import javax.servlet.http.HttpServletRequest;

import org.onap.logging.ref.slf4j.analysis.CallGraphAnalyzer;
import org.onap.logging.ref.slf4j.analysis.CallGraphReportWriter;
import org.onap.logging.ref.slf4j.analysis.LogEntry;
import org.onap.logging.ref.slf4j.demo.bean.Request;
import org.onap.logging.ref.slf4j.demo.bean.Response;
import org.onap.logging.ref.slf4j.demo.component.AbstractComponentTest;
import org.onap.logging.ref.slf4j.demo.component.alpha.ComponentAlpha;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Simple verification that we can easily get a call graph out of
 * some calls to logging via <tt>ONAPLogAdapter</tt>.
 */
public class CallGraphTest {

    /** Temporary directory into which logfiles are written. */
    private static File sDir;

    @BeforeSuite
    public static void setUp() throws Exception {
        AbstractComponentTest.setInProcess();
        sDir = Files.createTempDirectory(CallGraphTest.class.getName()).toFile();
        System.getProperties().setProperty("SLF4J_OUTPUT_DIRECTORY", sDir.getAbsolutePath());
        LoggerFactory.getLogger(CallGraphTest.class).info("Starting.");
    }

    @AfterSuite
    public static void tearDown() throws Exception {
        LoggerFactory.getLogger(CallGraphTest.class).info("Ending.");
        Thread.sleep(1000L);
        if (sDir != null) {
            System.err.println("Should be deleting [" + sDir.getAbsolutePath() + "]...");
        }
    }

    @Test(enabled = false)
    public void testSimple() throws Exception {

        final HttpServletRequest mock = new MockHttpServletRequest();
        final ComponentAlpha a = new ComponentAlpha();
        final Request request = new Request();
        final Response response = a.execute(request, mock);
        assertThat(response.getResponses().size(), is(0));
    }

    /**
     * A more complex (interesting) example of generating a call graph.
     * @throws Exception test failure.
     */
    @Test
    public void testComplex() throws Exception {

        Assert.assertNotNull(sDir);

        // Fan out some requests between test components.

        final Request a = new Request();
        a.setService("alpha");

        final Request b = new Request();
        b.setService("beta");

        final Request ac = new Request();
        ac.setService("gamma");

        final Request ad = new Request();
        ad.setService("delta");

        final Request bc1 = new Request();
        bc1.setService("gamma");

        final Request bc2 = new Request();
        bc2.setService("gamma");

        a.getRequests().add(b);
        a.getRequests().add(ac);
        a.getRequests().add(ad);
        b.getRequests().add(bc1);
        b.getRequests().add(bc2);

        // Deeper.

        final Request xb = new Request();
        xb.setService("beta");

        final Request xg = new Request();
        xg.setService("gamma");

        final Request xd = new Request();
        xd.setService("delta");

        a.getRequests().add(xb);
        xb.getRequests().add(xg);
        xg.getRequests().add(xd);

        // Execute.

        final HttpServletRequest mock = new MockHttpServletRequest();
        final ComponentAlpha component = new ComponentAlpha();
        final Response response = component.execute(a, mock);
        System.err.println(response);

        assertThat(response.getResponses().size(), is(4));

        Thread.sleep(1000L);

        // Find logfile.

        File log = null;
        for (final File candidate : sDir.listFiles()) {
            if (candidate.getName().endsWith(".log")) {
                log = candidate;
                break;
            }
        }

        Assert.assertNotNull(log);

        System.err.println("READING LOGFILE: " + log.getAbsolutePath());

        final CallGraphAnalyzer analyzer = new CallGraphAnalyzer();
        try (final BufferedReader reader = new BufferedReader(new FileReader(log))) {
            while (true) {

                final String line = reader.readLine();
                if (line == null) {
                    break;
                }

                final LogEntry entry = new LogEntry(line);
                analyzer.add(entry);
            }
        }

        //
        // Debug during dev, but annoying the rest of the time.
        //
        // System.err.println("--------------------------------------------------");
        // for (final LogEntry e : analyzer.getEntries()) {
        //     System.err.println(e.toShortString());
        // }
        // System.err.println("--------------------------------------------------");

        final CallGraphReportWriter writer = new CallGraphReportWriter(analyzer);
        final String shortReport = writer.getShortReport();
        final String longReport = writer.getLongReport();

        // Dump long report.

        System.out.println("----\nGraph:\n\n" + longReport + "\n----");

        // Validate short report.

        assertThat("Alpha\n" +
                "    Beta\n" +
                "        Gamma\n" +
                "        Gamma\n" +
                "    Gamma\n" +
                "    Delta\n" +
                "    Beta\n" +
                "        Gamma\n" +
                "            Delta\n",
                is(shortReport));

        // Ensure output reaches System.xxx.

        Thread.sleep(1000L);
    }
}