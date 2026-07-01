package br.com.reportengine.api;

import br.com.reportengine.api.dto.GenerateReportRequest;
import br.com.reportengine.api.dto.ReportDefinitionDTO;
import br.com.reportengine.api.dto.ReportSummaryDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportApiController {

    private final ReportCatalogService catalogService;
    private final ReportGenerationService generationService;

    @GetMapping
    public List<ReportSummaryDTO> listReports() {
        return catalogService.listActiveReports();
    }

    @GetMapping("/{cdRelatorio}/definition")
    public ReportDefinitionDTO getDefinition(@PathVariable String cdRelatorio) {
        return catalogService.getDefinition(cdRelatorio);
    }

    @PostMapping("/{cdRelatorio}/generate")
    public ResponseEntity<byte[]> generate(
            @PathVariable String cdRelatorio,
            @Valid @RequestBody GenerateReportRequest request,
            @RequestHeader(value = "X-Requested-By", required = false) String nmSolicitante
    ) {
        ReportGenerationService.GeneratedReport generated = generationService.generate(
                cdRelatorio,
                request,
                nmSolicitante
        );

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(generated.nmArquivo(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(generated.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(generated.content());
    }
}
