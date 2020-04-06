package kr.co.shineware.nlp.komoran.admin.exception;

public class ServerErrorException extends GlobalBaseException {
    public ServerErrorException() {
        this(null, null);
    }

    public ServerErrorException(String message) {
        this(message, null);
    }

    public ServerErrorException(Throwable cause) {
        this(null, cause);
    }

    public ServerErrorException(String message, Throwable cause) {
        super(ErrorType.BAD_REQUEST, String.format("Server Error : [%s]", message), cause);
    }
}
