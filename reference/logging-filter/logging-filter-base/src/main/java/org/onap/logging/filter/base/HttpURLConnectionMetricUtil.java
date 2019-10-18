package org.onap.logging.filter.base;

import java.net.HttpURLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpURLConnectionMetricUtil
        extends AbstractMetricLogFilter<HttpURLConnection, HttpURLConnection, HttpURLConnection> {
    protected static final Logger logger = LoggerFactory.getLogger(HttpURLConnectionMetricUtil.class);

    public void logBefore(HttpURLConnection request, ONAPComponentsList targetEntity) {
        setTargetEntity(targetEntity);
        pre(request, request);
    }

    public void logAfter(HttpURLConnection request) {
        post(request, request);
    }

    @Override
    protected String getTargetServiceName(HttpURLConnection request) {
        return request.getURL().getPath();
    }

    @Override
    protected int getHttpStatusCode(HttpURLConnection response) {
        try {
            return response.getResponseCode();
        } catch (Exception e) {
            logger.error("getHttpStatusCode failed, defaulting to 500", e);
        }
        return 500;
    }

    @Override
    protected String getResponseCode(HttpURLConnection response) {
        try {
            return String.valueOf(response.getResponseCode());
        } catch (Exception e) {
            logger.error("getResponseCode failed, defaulting to 500", e);
        }
        return "500";
    }

    @Override
    protected String getTargetEntity(HttpURLConnection request) {
        return Constants.DefaultValues.UNKNOWN_TARGET_ENTITY;
    }

    public void pre(HttpURLConnection request) {
        pre(request, null);
    }

    public void filter(HttpURLConnection request, HttpURLConnection response) {
        post(request, response);
    }

    @Override
    protected void addHeader(HttpURLConnection request, String headerName, String headerValue) {
        request.setRequestProperty(headerName, headerValue);
    }

    @Override
    protected String getServiceName(HttpURLConnection request) {
        return request.getURL().getPath();
    }

}
