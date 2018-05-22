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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring launcher, for testing invocations via REST.
 */
@SpringBootApplication
public class SLF4JRefApplication {

    /** System property override, read by embedded Logback configuration. */
    public static final String SLF4J_OUTPUT_DIRECTORY = "SLF4J_OUTPUT_DIRECTORY";

    /**
     * Launch from CLI.
     * @param args command-line args.
     * @throws Exception launch error.
     */
    public static void main(final String[] args) throws Exception {
        initOutputDirectory();
        SpringApplication.run(SLF4JRefApplication.class, args);
    }

    /**
     * Make sure the output directory has a default value. (It'll be
     * overridden by tests, but not in services.)
     */
    static void initOutputDirectory() {
        System.getProperties().setProperty(SLF4J_OUTPUT_DIRECTORY,
                System.getProperty(SLF4J_OUTPUT_DIRECTORY, "."));
    }
}