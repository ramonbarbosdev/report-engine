package br.com.reportengine.core;

import java.util.ArrayList;
import java.util.List;

public record ReportQueryValidationResult(List<String> errors, List<String> warnings) {

    public ReportQueryValidationResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public static ReportQueryValidationResult empty() {
        return new ReportQueryValidationResult(List.of(), List.of());
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public ReportQueryValidationResult merge(ReportQueryValidationResult other) {
        List<String> mergedErrors = new ArrayList<>(errors);
        mergedErrors.addAll(other.errors);
        List<String> mergedWarnings = new ArrayList<>(warnings);
        mergedWarnings.addAll(other.warnings);
        return new ReportQueryValidationResult(mergedErrors, mergedWarnings);
    }

    public void throwIfHasErrors() {
        if (!hasErrors()) {
            return;
        }
        throw new ReportValidationException(
                "SQL invalido: " + String.join(" ", errors),
                errors,
                warnings
        );
    }

    public void throwIfHasErrors(String queryName) {
        if (!hasErrors()) {
            return;
        }
        List<String> prefixed = errors.stream()
                .map(error -> "[" + queryName + "] " + error)
                .toList();
        throw new ReportValidationException(
                "SQL invalido na query '" + queryName + "': " + String.join(" ", errors),
                prefixed,
                warnings
        );
    }
}
