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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;

public class JoinPointMock implements JoinPoint {
    
    Object target;
    Object[] args;
    
    public void setTarget(Object aTarget) {
        target = aTarget;
    }

    public void setArgs(Object[] _args) {
        args = _args;
    }
    
    @Override
    public String toShortString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toLongString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getThis() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public Signature getSignature() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SourceLocation getSourceLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getKind() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StaticPart getStaticPart() {
        // TODO Auto-generated method stub
        return null;
    }

}
