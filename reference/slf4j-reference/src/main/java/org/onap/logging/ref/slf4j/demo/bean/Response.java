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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Test class, describing an outcome that should be reported.
 */
public class Response extends  AbstractBean {

    /** Delegate responses. */
    private final List<Response> mResponses = new ArrayList<>();

    /**
     * Get delegate responses.
     * @return responses.
     */
    public List<Response> getResponses() {
        return mResponses;
    }

    /**
     * Parse from serialized form.
     * @param in JSON.
     * @return parsed.
     */
    public static Response fromJSON(final JSONObject in) {
        final Response request = new Response();
        request.setCode(in.optString("code"));
        request.setSeverity(in.optString("severity"));
        final JSONArray responses = in.optJSONArray("responses");
        if (responses != null) {
            for (int i = 0 ; i < responses.length() ; i++) {
                request.getResponses().add(Response.fromJSON(responses.getJSONObject(i)));
            }
        }
        return request;
    }
}
