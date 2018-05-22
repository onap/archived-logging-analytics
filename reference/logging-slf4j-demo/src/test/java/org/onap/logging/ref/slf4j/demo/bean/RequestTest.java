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
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RequestTest {

    @Test
    public void testRoundtrip() {

        final Request in = new Request();
        in.setCode("code0");
        in.setService("service0");
        in.setSeverity("severity0");

        final Request childA = new Request();
        childA.setCode("codeA");
        childA.setService("serviceA");
        childA.setSeverity("severityA");

        final Request childB = new Request();
        childB.setCode("codeB");
        childB.setService("serviceB");
        childB.setSeverity("severityB");

        in.getRequests().add(childA);
        in.getRequests().add(childB);

        System.out.println(in.toString());
        System.out.println(new JSONObject(in.toString()).toString());

        final Request out = Request.fromJSON(new JSONObject(in.toString()));
        assertThat(out.getCode(), is(in.getCode()));
        assertThat(out.getService(), is(in.getService()));
        assertThat(out.getSeverity(), is(in.getSeverity()));
        assertThat(out.getRequests().size(), is(2));
        assertThat(out.getRequests().get(0).getCode(), is("codeA"));
        assertThat(out.getRequests().get(1).getCode(), is("codeB"));
    }
}
