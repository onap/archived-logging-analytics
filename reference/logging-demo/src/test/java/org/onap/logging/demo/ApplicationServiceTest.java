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
package org.onap.logging.demo;

import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.onap.demo.logging.ApplicationService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.Assert;

public class ApplicationServiceTest {

    @Test
    public final void testHealth() {
        ApplicationService service = new ApplicationService();
        Assert.notNull(service);
        HttpServletRequest servletRequest = new MockHttpServletRequest();
        Assert.notNull(servletRequest);
        boolean health = service.health(servletRequest);
        Assert.isTrue(health);
        System.out.println("health : " + health);
    }

}
