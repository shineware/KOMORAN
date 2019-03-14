package kr.co.shineware.nlp.komoran.admin.exception;

public class GlobalBaseException extends RuntimeException {
    private ErrorType type;
    private String message;

    public GlobalBaseException() {
        this(ErrorType.UNKNOWN, null, null);
    }

    public GlobalBaseException(String message) {
        this(ErrorType.UNKNOWN, message, null);
    }

    public GlobalBaseException(String message, Throwable cause) {
        this(ErrorType.UNKNOWN, message, cause);
    }

    public GlobalBaseException(Throwable cause) {
        this(ErrorType.UNKNOWN, cause.getMessage(), cause);
    }

    public GlobalBaseException(ErrorType type, String message) {
        this(type, message, null);
    }

    public GlobalBaseException(ErrorType type, Throwable cause) {
        this(type, cause.getMessage(), cause);
    }

    public GlobalBaseException(ErrorType type, String message, Throwable cause) {
        super(message, cause);

        if (type == null) {
            this.type = ErrorType.UNKNOWN;
        }

        this.type = type;
    }

    public ErrorType getType() {
        return type;
    }
}
