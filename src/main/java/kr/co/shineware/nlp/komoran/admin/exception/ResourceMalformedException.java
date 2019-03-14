package kr.co.shineware.nlp.komoran.admin.exception;

public class ResourceMalformedException extends GlobalBaseException {
    public ResourceMalformedException(int invalidResource) {
        this(String.valueOf(invalidResource), null);
    }

    public ResourceMalformedException(String invalidResource) {
        this(invalidResource, null);
    }

    public ResourceMalformedException(Throwable cause) {
        this(null, cause);
    }

    public ResourceMalformedException(String invalidResource, Throwable cause) {
        super(ErrorType.BAD_REQUEST, String.format("잘못된 [%s]", invalidResource), cause);
    }
}
