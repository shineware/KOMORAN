package kr.co.shineware.nlp.komoran.admin.exception;

public class ResourceNotFoundException extends GlobalBaseException {
    public ResourceNotFoundException(int resource) {
        this(String.valueOf(resource), null);
    }

    public ResourceNotFoundException(String resource) {
        this(resource, null);
    }

    public ResourceNotFoundException(Throwable cause) {
        this(null, cause);
    }

    public ResourceNotFoundException(String resource, Throwable cause) {
        super(ErrorType.NOT_FOUND, String.format("Resource Not Found : [%s]", resource), cause);
    }
}
