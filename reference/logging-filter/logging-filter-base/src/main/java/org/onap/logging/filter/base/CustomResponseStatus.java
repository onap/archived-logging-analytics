package org.onap.logging.filter.base;

public enum CustomResponseStatus {
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity");

    private final int code;
    private final String reason;

    CustomResponseStatus(int statusCode, String reasonPhrase) {
        this.code = statusCode;
        this.reason = reasonPhrase;
    }

    public static CustomResponseStatus fromStatusCode(int statusCode) {
        for (CustomResponseStatus s : values()) {
            if (s.code == statusCode) {
                return s;
            }
        }

        return null;
    }

    public int getStatusCode() {
        return this.code;
    }

    public String getReasonPhrase() {
        return this.toString();
    }

    public String toString() {
        return this.reason;
    }
}
