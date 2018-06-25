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

//import org.onap.logging.ref.slf4j.ONAPLogAdapter;
//import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
@Service("daoFacade")
public class ApplicationService implements ApplicationServiceLocal {
	
    @Override
    public Boolean health(HttpServletRequest servletRequest) {
    	Boolean health = true;
    	// TODO: check database
    	//LoggerFactory.getLogger(ApplicationService.class).info("Start /health");
    	//final ONAPLogAdapter adapter = new ONAPLogAdapter(LoggerFactory.getLogger(this.getClass()));
    	//try {
    	//    adapter.entering(new ONAPLogAdapter.HttpServletRequestAdapter(servletRequest));
    	//} finally {
    	//    adapter.exiting();
    	//}
    	
    	return health;
    }
  
}
