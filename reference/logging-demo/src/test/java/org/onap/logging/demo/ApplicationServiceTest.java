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

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.onap.demo.logging.test.JoinPointMock;
import org.onap.demo.logging.ApplicationService;
import org.onap.logging.library.LoggingAspect;
import org.onap.demo.logging.RestApplication;
import org.onap.demo.logging.RestHealthServiceImpl;
import org.onap.demo.logging.RestServiceImpl;
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
    }

    @Test
    public final void testRestEndpointCoverageForRestHealthServiceImpl() {
        // primarily for code coverage
        RestHealthServiceImpl service = new RestHealthServiceImpl();
        Assert.notNull(service);
        ApplicationService appService = new ApplicationService();
        Assert.notNull(appService);
        service.setApplicationService(appService);
        String health = service.getHealth();
        Assert.notNull(health);
        Assert.isTrue(health.equalsIgnoreCase("true"));
    }
    
    @Test
    public final void testRestEndpointCoverageForRestServiceImpl() {
        // primarily for code coverage
        RestServiceImpl service = new RestServiceImpl();
        Assert.notNull(service);
        ApplicationService appService = new ApplicationService();
        Assert.notNull(appService);
        service.setApplicationService(appService);
        String health = service.getTest();
        Assert.notNull(health);
    }
    
    @Test
    public final void testJAXRSFramework() {
        // primarily for code coverage
        RestApplication app = new RestApplication();
        Assert.notNull(app);
        Set<Class<?>> classes = app.getClasses();
        Assert.notNull(classes);
        Assert.isTrue(classes.size() > 1);
    }
    
    @Test
    public final void testLoggingAspect() {
        // primarily for code coverage
        LoggingAspect aspect = new LoggingAspect();
        Assert.notNull(aspect);
        JoinPointMock joinPoint = new JoinPointMock();
        ApplicationService appService = new ApplicationService();
        Assert.notNull(appService);
        joinPoint.setTarget(appService.getClass());
        joinPoint.getTarget();
        HttpServletRequest servletRequest = new MockHttpServletRequest();
        Assert.notNull(servletRequest);
        HttpServletRequest[] args = new HttpServletRequest[1];
        args[0] = servletRequest;
        joinPoint.setArgs(args);
        aspect.logAfter(joinPoint);
        aspect.logBefore(joinPoint);
        // cover mock joinpoint - expecting null as only target and args needs to be implemented
        joinPoint.getKind();
        joinPoint.getSignature();
        joinPoint.getSourceLocation();
        joinPoint.getStaticPart();
        joinPoint.getThis();
        joinPoint.toLongString();
        joinPoint.toShortString();
    }
}
