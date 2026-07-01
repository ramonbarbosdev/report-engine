package br.com.reportengine.core;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class ReportEngineException {

    private ReportEngineException() {
    }

    public static ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    public static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    public static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
