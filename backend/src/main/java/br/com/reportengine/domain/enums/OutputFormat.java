package br.com.reportengine.domain.enums;

public enum OutputFormat {
    PDF("application/pdf", "pdf"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");

    private final String contentType;
    private final String extension;

    OutputFormat(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String getContentType() {
        return contentType;
    }

    public String getExtension() {
        return extension;
    }

    public static OutputFormat fromString(String value) {
        return OutputFormat.valueOf(value.trim().toUpperCase());
    }
}
