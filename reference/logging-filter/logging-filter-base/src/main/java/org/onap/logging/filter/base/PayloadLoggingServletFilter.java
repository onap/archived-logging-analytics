/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.logging.filter.base;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.core.HttpHeaders;

public class PayloadLoggingServletFilter implements Filter {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PayloadLoggingServletFilter.class);
    private static final String REDACTED = "***REDACTED***";

    private static class ByteArrayServletStream extends ServletOutputStream {
        ByteArrayOutputStream baos;

        ByteArrayServletStream(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        @Override
        public void write(int param) throws IOException {
            baos.write(param);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener arg0) {
            // this method does nothing
        }
    }

    private static class ByteArrayPrintWriter extends PrintWriter {
        private ByteArrayOutputStream baos;
        private int errorCode = -1;
        private String errorMsg = "";
        private boolean errored = false;

        public ByteArrayPrintWriter(ByteArrayOutputStream out) {
            super(out);
            this.baos = out;
        }

        public ServletOutputStream getStream() {
            return new ByteArrayServletStream(baos);
        }

        public Boolean hasErrored() {
            return errored;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setError(int code) {
            errorCode = code;
            errored = true;
        }

        public void setError(int code, String msg) {
            errorMsg = msg;
            errorCode = code;
            errored = true;
        }

    }

    private class BufferedServletInputStream extends ServletInputStream {
        ByteArrayInputStream bais;

        public BufferedServletInputStream(ByteArrayInputStream bais) {
            this.bais = bais;
        }

        @Override
        public int available() {
            return bais.available();
        }

        @Override
        public int read() {
            return bais.read();
        }

        @Override
        public int read(byte[] buf, int off, int len) {
            return bais.read(buf, off, len);
        }

        @Override
        public boolean isFinished() {
            return available() < 1;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener arg0) {
            // this method does nothing
        }

    }

    private class BufferedRequestWrapper extends HttpServletRequestWrapper {
        ByteArrayInputStream bais;
        ByteArrayOutputStream baos;
        BufferedServletInputStream bsis;
        byte[] buffer;

        public BufferedRequestWrapper(HttpServletRequest req) throws IOException {
            super(req);

            InputStream is = req.getInputStream();
            baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int letti;
            while ((letti = is.read(buf)) > 0) {
                baos.write(buf, 0, letti);
            }
            buffer = baos.toByteArray();
        }

        @Override
        public ServletInputStream getInputStream() {
            try {
                bais = new ByteArrayInputStream(buffer);
                bsis = new BufferedServletInputStream(bais);
            } catch (Exception ex) {
                log.error("Exception in getInputStream", ex);
            }
            return bsis;
        }

        public byte[] getBuffer() {
            return buffer;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // this method does nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        BufferedRequestWrapper bufferedRequest = new BufferedRequestWrapper(httpRequest);

        StringBuilder requestHeaders = new StringBuilder("REQUEST|");
        requestHeaders.append(httpRequest.getMethod());
        requestHeaders.append(":");
        requestHeaders.append(httpRequest.getRequestURL().toString());
        requestHeaders.append("|");
        requestHeaders.append(getSecureRequestHeaders(httpRequest));
        log.info(requestHeaders.toString());

        log.info("REQUEST BODY|" + new String(bufferedRequest.getBuffer()));

        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ByteArrayPrintWriter pw = new ByteArrayPrintWriter(baos);

        HttpServletResponse wrappedResp = new HttpServletResponseWrapper(response) {
            @Override
            public PrintWriter getWriter() {
                return pw;
            }

            @Override
            public ServletOutputStream getOutputStream() {
                return pw.getStream();
            }

            @Override
            public void sendError(int sc) throws IOException {
                super.sendError(sc);
                pw.setError(sc);

            }

            @Override
            public void sendError(int sc, String msg) throws IOException {
                super.sendError(sc, msg);
                pw.setError(sc, msg);
            }
        };

        try {
            filterChain.doFilter(bufferedRequest, wrappedResp);
        } catch (Exception e) {
            log.error("Chain Exception", e);
            throw e;
        } finally {
            try {
                byte[] bytes = baos.toByteArray();
                StringBuilder responseHeaders = new StringBuilder("RESPONSE HEADERS|");
                responseHeaders.append(formatResponseHeaders(response));
                responseHeaders.append("Status:");
                responseHeaders.append(response.getStatus());
                responseHeaders.append(";IsCommited:" + wrappedResp.isCommitted());

                log.info(responseHeaders.toString());

                if ("gzip".equals(response.getHeader("Content-Encoding"))) {
                    log.info("UNGZIPED RESPONSE BODY|" + decompressGZIPByteArray(bytes));
                } else {
                    log.info("RESPONSE BODY|" + new String(bytes));
                }

                if (pw.hasErrored()) {
                    log.info("ERROR RESPONSE|" + pw.getErrorCode() + ":" + pw.getErrorMsg());
                } else {
                    if (!wrappedResp.isCommitted()) {
                        response.getOutputStream().write(bytes);
                        response.getOutputStream().flush();
                    }
                }
            } catch (Exception e) {
                log.error("Exception in response filter", e);
            }
        }
    }

    @Override
    public void destroy() {
        // this method does nothing
    }

    private String decompressGZIPByteArray(byte[] bytes) {
        BufferedReader in = null;
        InputStreamReader inR = null;
        ByteArrayInputStream byteS = null;
        GZIPInputStream gzS = null;
        StringBuilder str = new StringBuilder();
        try {
            byteS = new ByteArrayInputStream(bytes);
            gzS = new GZIPInputStream(byteS);
            inR = new InputStreamReader(gzS);
            in = new BufferedReader(inR);

            if (in != null) {
                String content;
                while ((content = in.readLine()) != null) {
                    str.append(content);
                }
            }

        } catch (Exception e) {
            log.error("Failed get read GZIPInputStream", e);
        } finally {
            if (byteS != null)
                try {
                    byteS.close();
                } catch (IOException e1) {
                    log.error("Failed to close ByteStream", e1);
                }
            if (gzS != null)
                try {
                    gzS.close();
                } catch (IOException e2) {
                    log.error("Failed to close GZStream", e2);
                }
            if (inR != null)
                try {
                    inR.close();
                } catch (IOException e3) {
                    log.error("Failed to close InputReader", e3);
                }
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("Failed to close BufferedReader", e);
                }
        }
        return str.toString();
    }

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
