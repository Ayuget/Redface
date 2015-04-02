package com.ayuget.redface.data.api.model;

public class Response {
    private final boolean successful;

    private final ResponseCode code;

    private Response(boolean successful, ResponseCode code) {
        this.successful = successful;
        this.code = code;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public ResponseCode getCode() {
        return code;
    }

    public static Response buildSuccess(ResponseCode code) {
        return new Response(true, code);
    }

    public static Response buildFailure(ResponseCode code) {
        return new Response(false, code);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Response{");
        sb.append("successful=").append(successful);
        sb.append(", code=").append(code);
        sb.append('}');
        return sb.toString();
    }
}
