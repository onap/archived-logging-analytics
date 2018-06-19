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

package org.onap.logging.ref.slf4j.demo;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Tests for {@link SLF4JRefApplication}.
 */
public class SLF4JRefApplicationTest {

    @Test
    public void testProperty() {
        assertThat(SLF4JRefApplication.TESTNG_SLF4J_OUTPUT_DIRECTORY,
                is("TESTNG_SLF4J_OUTPUT_DIRECTORY"));
    }

    @Test
    public void testInitOutputDirectory() throws Exception {
        SLF4JRefApplication.initOutputDirectory();
        assertThat(System.getProperty(SLF4JRefApplication.TESTNG_SLF4J_OUTPUT_DIRECTORY),
                notNullValue());
    }
}
