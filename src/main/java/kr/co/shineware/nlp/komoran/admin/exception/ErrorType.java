package kr.co.shineware.nlp.komoran.admin.exception;

public enum ErrorType {

    UNKNOWN("UNKNOWN", "msg.error.unknown"),
    SERVER_ERROR("ERR_SERVER", "msg.error.server"),
    NOT_FOUND("ERR_NOT_FOUND", "msg.error.notfound"),
    BAD_REQUEST("ERR_INVALID_REQUEST", "msg.error.invalidrequest");

    String type;
    String resource;

    ErrorType(String type) {
        this.type = type;
    }

    ErrorType(String type, String resource) {
        this.type = type;
        this.resource = resource;
    }

    public String getCode() {
        return type;
    }

    public String getResource() {
        return resource;
    }
}