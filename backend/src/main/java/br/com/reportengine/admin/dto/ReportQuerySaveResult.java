package br.com.reportengine.admin.dto;

import java.util.List;

public record ReportQuerySaveResult(
        ReportAdminDetailDTO detail,
        List<String> warnings
) {
}
