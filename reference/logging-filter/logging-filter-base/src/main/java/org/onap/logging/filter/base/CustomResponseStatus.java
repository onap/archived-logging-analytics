/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.logging.filter.base;

public enum CustomResponseStatus {
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity");

    private final int code;
    private final String reason;

    CustomResponseStatus(int statusCode, String reasonPhrase) {
        this.code = statusCode;
        this.reason = reasonPhrase;
    }

    public static CustomResponseStatus fromStatusCode(int statusCode) {
        for (CustomResponseStatus s : values()) {
            if (s.code == statusCode) {
                return s;
            }
        }

        return null;
    }

    public int getStatusCode() {
        return this.code;
    }

    public String getReasonPhrase() {
        return this.toString();
    }

    public String toString() {
        return this.reason;
    }
}
