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

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.event.Level;

/**
 * Test class for reading a logentry during analysis.
 */
public class LogEntry {

    /** Property. */
    private final Date mTimestamp;

    /** Property. */
    private final String mThread;

    /** Property. */
    private final Level mLevel;

    /** Property. */
    private final String mLogger;

    /** Property. */
    private final String mMessage;

    /** Property. */
    private final String mException;

    /** Property. */
    private final Map<String, String> mMDCs;

    /** Property. */
    private final String mMarkers;

    /**
     * Construct from log line.
     * @param line to be parsed.
     */
    public LogEntry(final String line) {

        final String [] tokens = line.split("\t", -1);
        if (tokens.length < 8) {
            throw new IllegalArgumentException("Unsupported line (expected 8+ tokens, got "
                    + tokens.length + "): " + line);
        }

        int index = 0;

        this.mTimestamp = DatatypeConverter.parseDateTime(tokens[index++]).getTime();
        this.mThread = tokens[index++];
        this.mLevel = Level.valueOf(tokens[index++].trim());
        this.mLogger = tokens[index++];

        this.mMDCs = parseMDCs(tokens[index++]);
        this.mMessage = tokens[index++];
        this.mException = tokens[index++];
        this.mMarkers = tokens[index++];
    }

    /**
     * Parse serialized MDCs.
     * @param mdc serialized DMC map.
     * @return parsed.
     */
    static Map<String, String> parseMDCs(final String mdc) {

        final Map<String, String> mdcs = new HashMap<>();
        for (final String token : mdc.split(",")) {
            final String[] mdcTokens = token.split("=");
            if (mdcTokens.length == 2) {
                mdcs.put(StringUtils.trim(mdcTokens[0]), StringUtils.trim(mdcTokens[1]));
            }
        }
        return Collections.unmodifiableMap(mdcs);
    }

    /**
     * Getter.
     * @return property.
     */
    public Date getTimestamp() {
        return this.mTimestamp;
    }

    /**
     * Getter.
     * @return property.
     */
    public String getThread() {
        return this.mThread;
    }

    /**
     * Getter.
     * @return property.
     */
    public Level getLevel() {
        return this.mLevel;
    }

    /**
     * Getter.
     * @return property.
     */
    public String getLogger() {
        return this.mLogger;
    }

    /**
     * Getter.
     * @return property.
     */
    public String getMessage() {
        return this.mMessage;
    }

    /**
     * Getter.
     * @return property.
     */
    public String getException() {
        return this.mException;
    }

    /**
     * Getter.
     * @return property.
     */
    public Map<String, String> getMDCs() {
        return this.mMDCs;
    }

    /**
     * Getter.
     * @return property.
     */
    public String getMarkers() {
        return this.mMarkers;
    }

    /**
     * Getter.
     * @return property.
     */
    public String getRequestID() {
        return this.getMDCs().get("RequestID");
    }

    /**
     * Getter.
     * @return property.
     */
    public String getInvocationID() {
        return this.getMDCs().get("InvocationID");
    }

    /**
     * Getter.
     * @return property.
     */
    public String getPartnerName() {
        return this.getMDCs().get("PartnerName");
    }

    /**
     * Getter.
     * @return property.
     */
    public String getInvokingID() {
        if (StringUtils.defaultString(this.getMarkers()).startsWith("INVOKE")) {
            return this.getMessage();
        }
        return null;
    }

    /**
     * Getter.
     * @return property.
     */
    public String toShortString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("LogEntry(markers=").append(StringUtils.defaultString(this.getMarkers()));
        buf.append(", logger=").append(this.getLogger().substring(1 + this.getLogger().lastIndexOf(".")));
        if (StringUtils.isNotBlank(this.getRequestID())) {
            buf.append(", requestID=[...]").append(StringUtils.right(this.getRequestID(), 8));
        }
        if (StringUtils.isNotBlank(this.getInvocationID())) {
            buf.append(", invocationID=[...]").append(StringUtils.right(this.getInvocationID(), 8));
        }
        if (StringUtils.isNotBlank(this.getInvokingID())) {
            buf.append(", invokingID=[...]").append(StringUtils.right(this.getInvokingID(), 8));
        }

        final Calendar c = Calendar.getInstance();
        c.setTime(this.getTimestamp());

        buf.append(", timestamp=").append(DatatypeConverter.printDateTime(c));
        return buf.append(")").toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
