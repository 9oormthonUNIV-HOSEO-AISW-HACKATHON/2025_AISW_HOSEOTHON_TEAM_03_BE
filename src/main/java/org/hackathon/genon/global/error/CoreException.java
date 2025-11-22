package org.hackathon.genon.global.error;

import lombok.Getter;

@Getter
public class CoreException extends RuntimeException {

    private final ErrorStatus errorStatus;

    public CoreException(ErrorStatus status) {
        super(status.getMessage());
        this.errorStatus = status;
    }

    public CoreException(String message) {
        super(message);
        this.errorStatus = null;
    }

    public CoreException(ErrorStatus status, Throwable cause) {
        super(status.getMessage(), cause);
        this.errorStatus = status;
    }

}