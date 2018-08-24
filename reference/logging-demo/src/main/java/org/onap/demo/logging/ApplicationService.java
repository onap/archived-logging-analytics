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
package org.onap.demo.logging;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
@Service("daoFacade")
/**
 * Run with http://localhost:8080/logging-demo/rest/health/health
 *
 */
public class ApplicationService implements ApplicationServiceLocal {
	
    @Override
    public Boolean health(HttpServletRequest servletRequest) {
    	Boolean health = true;
    	// TODO: check database
    	// Log outside the AOP framework - to simulate existing component logs between the ENTRY/EXIT markers
    	LoggerFactory.getLogger(this.getClass()).info("default appender - info level - Running /health");
    	// TODO: exit comes in at all times (entry only in info)
    	LoggerFactory.getLogger(this.getClass()).debug("default appender - debug level - Running /health");
    	
    	
    	return health;
    }
  
}
