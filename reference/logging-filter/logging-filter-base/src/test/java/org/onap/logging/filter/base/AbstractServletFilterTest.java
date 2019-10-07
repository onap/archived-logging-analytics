package org.onap.logging.filter.base;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AbstractServletFilterTest extends AbstractServletFilter {
    @Test
    public void getPublicAuthUser() {
        String value = "dXNlcjpwYXNz"; // decodes to user:pass
        String userName = getBasicAuthUserName(value);
        assertEquals("user", userName);
    }

}
