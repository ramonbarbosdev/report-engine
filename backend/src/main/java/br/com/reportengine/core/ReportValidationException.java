package br.com.reportengine.core;

import lombok.Getter;

import java.util.List;

@Getter
public class ReportValidationException extends RuntimeException {

    private final List<String> errors;
    private final List<String> warnings;

    public ReportValidationException(String message, List<String> errors, List<String> warnings) {
        super(message);
        this.errors = errors == null ? List.of() : List.copyOf(errors);
        this.warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
