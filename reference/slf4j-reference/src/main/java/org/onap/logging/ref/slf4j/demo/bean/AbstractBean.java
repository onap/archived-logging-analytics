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

package org.onap.logging.ref.slf4j.demo.bean;

import org.json.JSONObject;

/**
 * Base class for {@link Request} and {@link Response} beans, since
 * they're almost the same thing.
 */
public abstract class AbstractBean {

    /** Bean property. */
    private String mService;

    /** Bean property. */
    private String mCode;

    /** Bean property. */
    private String mSeverity;

    /**
     * Getter.
     * @return bean property.
     */
    public String getService() {
        return this.mService;
    }

    /**
     * Setter.
     * @param service bean property.
     */
    public void setService(final String service) {
        this.mService = service;
    }

    /**
     * Getter.
     * @return bean property.
     */
    public String getCode() {
        return this.mCode;
    }

    /**
     * Setter.
     * @param code bean property.
     */
    public void setCode(final String code) {
        this.mCode = code;
    }

    /**
     * Getter.
     * @return bean property.
     */
    public String getSeverity() {
        return this.mSeverity;
    }

    /**
     * Setter.
     * @param severity bean property.
     */
    public void setSeverity(final String severity) {
        this.mSeverity = severity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new JSONObject(this).toString(4);
    }
}
