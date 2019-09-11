package org.onap.logging.filter.base;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

public abstract class AbstractServletFilter {
    protected static final String REDACTED = "***REDACTED***";

    protected String getSecureRequestHeaders(HttpServletRequest httpRequest) {
        StringBuilder sb = new StringBuilder();
        String header;
        for (Enumeration<String> e = httpRequest.getHeaderNames(); e.hasMoreElements();) {
            header = e.nextElement();
            sb.append(header);
            sb.append(":");
            if (header.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                sb.append(REDACTED);
            } else {
                sb.append(httpRequest.getHeader(header));
            }
            sb.append(";");
        }
        return sb.toString();
    }

    protected String formatResponseHeaders(HttpServletResponse response) {
        StringBuilder sb = new StringBuilder();
        for (String headerName : response.getHeaderNames()) {
            sb.append(headerName);
            sb.append(":");
            sb.append(response.getHeader(headerName));
            sb.append(";");
        }
        return sb.toString();
    }
}
