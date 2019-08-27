/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import org.junit.After;
import org.junit.Test;
import org.onap.logging.filter.base.PropertyUtil;

public class PropertyUtilTest {

    private PropertyUtil propertyUtil = new PropertyUtil();

    @After
    public void tearDown() {
        System.clearProperty("partnerName");
    }

    @Test
    public void getPropertyTest() {
        System.setProperty("partnerName", "partnerName");

        String partnerName = propertyUtil.getProperty("partnerName");
        assertEquals("partnerName", partnerName);
    }

    @Test
    public void getPropertyNullTest() {
        String partnerName = propertyUtil.getProperty("partner");
        assertEquals("UNKNOWN", partnerName);
    }
}
