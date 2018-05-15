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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;

/**
 * A simple, recursive text-only report writer for the call graph.
 */
public class CallGraphReportWriter {

    /** The analyzer which does the work. */
    final CallGraphAnalyzer mAnalyzer;

    /** Short report, for validation. */
    final StringBuilder mShortReport = new StringBuilder();

    /** Longer report, for human eyes. */
    final StringBuilder mLongReport = new StringBuilder();

    /**
     * Construct writer.
     * @param analyzer initialized analyzer.
     */
    public CallGraphReportWriter(final CallGraphAnalyzer analyzer) {

        this.mAnalyzer = analyzer;

        Assert.assertTrue(analyzer.getEntries().size() > 0);
        final LogEntry e0 = analyzer.findEntryPoint();
        Assert.assertNotNull(e0);

        this.mLongReport.append(e0.toShortString()).append("\n");
        this.mShortReport.append(StringUtils.substringAfter(e0.getLogger(), ".Component")).append("\n");

        this.report(e0, 1);

    }

    /**
     * Recursively analyze.
     * @param invoker entry point.
     * @param depth recursive depth, for handbrake.
     */
    private void report(final LogEntry invoker, final int depth) {

        if (depth > 100) {
            throw new AssertionError("Recursion ad infinitum");
        }

        final List<LogEntry> invokes0 = this.mAnalyzer.findInvokes(invoker);
        for (final LogEntry invoke0 : invokes0) {

            final LogEntry invoked0 = this.mAnalyzer.findInvocation(invoke0);

            Assert.assertNotNull(invoked0);

            final String indent = StringUtils.repeat(' ', depth * 4);
            this.mLongReport.append(indent).append(invoked0.toShortString()).append('\n');
            this.mShortReport.append(indent).append(StringUtils.substringAfter(invoked0.getLogger(), ".Component")).append('\n');

            report(invoked0, depth + 1);
        }
    }

    /**
     * Get report.
     * @return short report, for validation.
     */
    public String getShortReport() {
        return this.mShortReport.toString();
    }

    /**
     * Get report.
     * @return long report, for printing out.
     */
    public String getLongReport() {
        return this.mLongReport.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.getLongReport();
    }
}
