package kr.co.shineware.nlp.komoran.admin.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.http.HttpStatus;

@JsonComponent
public class ResponseDetail {

    @JsonProperty
    private HttpStatus status;

    @JsonProperty
    private boolean error;

    @JsonProperty
    private String message;

    @JsonProperty
    private String details;

    @JsonProperty
    private Object data;


    public ResponseDetail() {
        this.status = HttpStatus.OK;
        this.error = false;
        this.message = null;
        this.details = null;
    }

    public HttpStatus getStatus() {
        return this.status;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public void setError(HttpStatus status) {
        this.error = true;
        this.status = status;
    }

    public void setError(String message) {
        this.error = true;
        this.message = message;
    }

    public void setError(HttpStatus status, String message) {
        this.error = true;
        this.status = status;
        this.message = message;
    }

    public void setError(HttpStatus status, String message, String details) {
        this.error = true;
        this.status = status;
        this.message = message;
        this.details = details;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
