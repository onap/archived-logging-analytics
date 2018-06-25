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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

@Aspect
public class LoggingAspect {
    
    @Before("execution(* org.onap.demo.logging.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Object servletRequest = args[0];
        ONAPLogAdapter.HttpServletRequestAdapter requestAdapter = new ONAPLogAdapter.HttpServletRequestAdapter(
                (HttpServletRequest)servletRequest);
        final ONAPLogAdapter adapter = new ONAPLogAdapter(LoggerFactory.getLogger(joinPoint.getTarget().getClass()));
        try {
            adapter.entering(requestAdapter);
        } finally {
            adapter.exiting();
        }
    }

}
