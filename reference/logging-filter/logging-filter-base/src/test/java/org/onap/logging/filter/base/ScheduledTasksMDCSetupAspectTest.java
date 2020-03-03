/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.After;
import org.junit.Test;
import org.onap.logging.filter.base.Constants;
import org.onap.logging.ref.slf4j.ONAPLogConstants;

import org.slf4j.MDC;

public class ScheduledTasksMDCSetupAspectTest {

    private ScheduledTasksMDCSetupAspect tasksMDCSetup = new ScheduledTasksMDCSetupAspect();

    @After
    public void tearDown() {
        MDC.clear();
        System.clearProperty("partnerName");
    }

    @Test
    public void mdcSetupTest() {
        System.setProperty("partnerName", ONAPComponents.SO.toString());
        tasksMDCSetup.setupMDC("mdcSetupTest");

        assertTrue(isValidUUID(MDC.get(ONAPLogConstants.MDCs.REQUEST_ID)));
        assertTrue(isValidUUID(MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID)));
        assertEquals(ONAPComponents.SO.toString(), MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
        assertEquals(ONAPComponents.SO.toString(), MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME));
        assertEquals("mdcSetupTest", MDC.get(ONAPLogConstants.MDCs.SERVICE_NAME));
        assertEquals(Constants.DefaultValues.UNKNOWN, MDC.get(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME));
        assertNotNull(MDC.get(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP));
        assertNotNull(MDC.get(ONAPLogConstants.MDCs.ELAPSED_TIME));
        assertNotNull(MDC.get(ONAPLogConstants.MDCs.LOG_TIMESTAMP));
        assertNotNull(MDC.get(ONAPLogConstants.MDCs.SERVER_FQDN));
    }

    @Test
    public void errorMDCSetupTest() {
        tasksMDCSetup.errorMDCSetup(ErrorCode.UnknownError, "Error");

        assertEquals("900", MDC.get(ONAPLogConstants.MDCs.ERROR_CODE));
        assertEquals("Error", MDC.get(ONAPLogConstants.MDCs.ERROR_DESC));
    }

    @Test
    public void setStatusCodeTest() {
        tasksMDCSetup.setStatusCode();

        assertEquals(ONAPLogConstants.ResponseStatus.COMPLETE.toString(),
                MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
    }

    @Test
    public void setStatusCodeErrorTest() {
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.ERROR.toString());
        tasksMDCSetup.setStatusCode();

        assertEquals(ONAPLogConstants.ResponseStatus.ERROR.toString(),
                MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
    }

    public static boolean isValidUUID(String id) {
        try {
            if (null == id) {
                return false;
            }
            UUID uuid = UUID.fromString(id);
            return uuid.toString().equalsIgnoreCase(id);
        } catch (IllegalArgumentException iae) {
            return false;
        }
    }
}
