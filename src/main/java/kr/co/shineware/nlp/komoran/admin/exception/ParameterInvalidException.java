package kr.co.shineware.nlp.komoran.admin.exception;

public class ParameterInvalidException extends GlobalBaseException {
    public ParameterInvalidException(int invalidResource) {
        this(String.valueOf(invalidResource), null);
    }

    public ParameterInvalidException(String invalidResource) {
        this(invalidResource, null);
    }

    public ParameterInvalidException(Throwable cause) {
        this(null, cause);
    }

    public ParameterInvalidException(String invalidResource, Throwable cause) {
        super(ErrorType.BAD_REQUEST, String.format("Invalid Request : [%s]", invalidResource), cause);
    }
}
