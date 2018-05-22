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

package org.onap.logging.ref.slf4j.demo.component.delta;

import java.util.UUID;

import org.onap.logging.ref.slf4j.demo.component.AbstractComponent;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Discrete service, identical to the others but with its own identifiers.
 */
@RequestMapping("/services/delta")
public class ComponentDelta extends AbstractComponent {

    /** Component instance UUID constant. */
    private static final String INSTANCE_UUID = UUID.randomUUID().toString();

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getId() {
        return "delta";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getInstanceUUID() {
        return INSTANCE_UUID;
    }
}
