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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Crude analyzer for log messages, to build a simple
 * representation of the call graph.
 */
public class CallGraphAnalyzer {

    /** Messages of interest. */
    private List<LogEntry> mEntries = new ArrayList<>();

    /**
     * Capture entry if it's interesting.
     * @param entry candidate.
     * @return this.
     */
    public CallGraphAnalyzer add(final LogEntry entry) {

        if (entry.getLogger().contains("ONAPLogAdapterTest")) {
            return this;
        }

        if (StringUtils.isNotBlank(entry.getMarkers())) {
            this.mEntries.add(entry);
        }

        return this;
    }

    /**
     * Get all captured entries, for diagnostics only.
     * @return entries.
     */
    public List<LogEntry> getEntries() {
        return this.mEntries;
    }

    /**
     * Find the entry point into the call graph through the various components.
     * @return entry point or (failure) null.
     */
    public LogEntry findEntryPoint() {
        for (final LogEntry e : this.mEntries) {
            if (e.getLogger().endsWith("ComponentAlpha")) {
                if ("ENTRY".equals(e.getMarkers())) {
                    if (StringUtils.isBlank(e.getPartnerName())) {
                        return e;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Find entries for where a component invokes others.
     * @param parent parent ENTRY (not actually the entry where it's doing the invoking).
     * @return components invoked by this one.
     */
    public List<LogEntry> findInvokes(final LogEntry parent) {
        final List<LogEntry> invokes = new ArrayList<>();
        for (final LogEntry e : this.mEntries) {
            if (StringUtils.equals(parent.getInvocationID(), e.getInvocationID())) {
                final String invokingID = e.getInvokingID();
                if (StringUtils.isNotBlank(invokingID)) {
                    invokes.add(e);
                }
            }
        }
        return invokes;
    }

    /**
     * Find a specific invocation.
     * @param invoke invocation record.
     * @return invocation ENTRY, or (failure) null if not found.
     */
    public LogEntry findInvocation(final LogEntry invoke) {
        for (final LogEntry e : this.mEntries) {
            if ("ENTRY".equals(e.getMarkers())) {
                if (StringUtils.equals(invoke.getInvokingID(), e.getInvocationID())) {
                    return e;
                }
            }
        }
        return null;
    }
}
