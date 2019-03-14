package kr.co.shineware.nlp.komoran.admin.exception;

import kr.co.shineware.nlp.komoran.admin.util.ResponseDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice
@RestController
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    private HttpStatus getHttpStatusFromErrorType(GlobalBaseException e) {
        if (e.getType() == ErrorType.UNKNOWN) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else if (e.getType() == ErrorType.SERVER_ERROR) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else if (e.getType() == ErrorType.NOT_FOUND) {
            return HttpStatus.NOT_FOUND;
        } else if (e.getType() == ErrorType.BAD_REQUEST) {
            return HttpStatus.BAD_REQUEST;
        } else {
            return HttpStatus.NOT_IMPLEMENTED;
        }
    }

    private ResponseEntity<?> handleGlobalBaseException(GlobalBaseException e) {
        ResponseDetail responseDetail = new ResponseDetail();
        responseDetail.setError(
                getHttpStatusFromErrorType(e),
                e.getMessage()
        );

        return new ResponseEntity<>(responseDetail, responseDetail.getStatus());
    }


    @ExceptionHandler(value = ParameterInvalidException.class)
    public ResponseEntity<?> handleParameterInvalidException(ParameterInvalidException e) {
        return handleGlobalBaseException(e);
    }

    @ExceptionHandler(value = ResourceDuplicatedException.class)
    public ResponseEntity<?> handleResourceDuplicatedException(ResourceDuplicatedException e) {
        return handleGlobalBaseException(e);
    }

    @ExceptionHandler(value = ResourceMalformedException.class)
    public ResponseEntity<?> handleResourceMalformedException(ResourceMalformedException e) {
        return handleGlobalBaseException(e);
    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException e) {
        return handleGlobalBaseException(e);
    }

    @ExceptionHandler(value = UnknownException.class)
    public ResponseEntity<?> handleUnknownException(UnknownException e) {
        return handleGlobalBaseException(e);
    }

    @ExceptionHandler(value = ServerErrorException.class)
    public ResponseEntity<?> handleServerErrorException(ServerErrorException e) {
        return handleGlobalBaseException(e);
    }

}
