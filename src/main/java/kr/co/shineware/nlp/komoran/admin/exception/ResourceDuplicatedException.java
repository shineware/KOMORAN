package kr.co.shineware.nlp.komoran.admin.exception;

public class ResourceDuplicatedException extends GlobalBaseException {
    public ResourceDuplicatedException(int resource) {
        this(String.valueOf(resource), null);
    }

    public ResourceDuplicatedException(String resource) {
        this(resource, null);
    }

    public ResourceDuplicatedException(Throwable cause) {
        this(null, cause);
    }

    public ResourceDuplicatedException(String resource, Throwable cause) {
        super(ErrorType.BAD_REQUEST, String.format("Duplicated Resource : [%s]", resource), cause);
    }
}
