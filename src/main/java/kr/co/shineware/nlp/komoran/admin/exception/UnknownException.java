package kr.co.shineware.nlp.komoran.admin.exception;

public class UnknownException extends GlobalBaseException {
    public UnknownException() {
        this(null, null);
    }

    public UnknownException(Throwable cause) {
        this(null, cause);
    }

    public UnknownException(String message, Throwable cause) {
        super(ErrorType.UNKNOWN, "Unknown Error", cause);
    }
}
