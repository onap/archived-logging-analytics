/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Test;
import org.onap.logging.ref.slf4j.ONAPLogConstants;

public class LoggingContainerFilterTest {
    private String invocationId = "4d31fe02-4918-4975-942f-fe51a44e6a9b";

    @Test
    public void convertMultivaluedMapToHashMap() {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(ONAPLogConstants.Headers.INVOCATION_ID, invocationId);
        SimpleMap result = new SimpleJaxrsHeadersMap(headers);
        assertEquals(invocationId, result.get(ONAPLogConstants.Headers.INVOCATION_ID));
    }
}
