package br.com.reportengine.admin;

import br.com.reportengine.admin.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class ReportAdminController {

    private final ReportAdminService adminService;

    @GetMapping
    public List<ReportSummaryAdminDTO> list() {
        return adminService.listAll();
    }

    @GetMapping("/{cdRelatorio}")
    public ReportAdminDetailDTO get(@PathVariable String cdRelatorio) {
        return adminService.getByCdRelatorio(cdRelatorio);
    }

    @PostMapping
    public ReportAdminDetailDTO create(@Valid @RequestBody ReportUpsertRequest request) {
        return adminService.create(request);
    }

    @PutMapping("/{cdRelatorio}")
    public ReportAdminDetailDTO update(
            @PathVariable String cdRelatorio,
            @Valid @RequestBody ReportUpsertRequest request
    ) {
        return adminService.update(cdRelatorio, request);
    }

    @PutMapping("/{cdRelatorio}/queries")
    public ReportAdminDetailDTO upsertQuery(
            @PathVariable String cdRelatorio,
            @Valid @RequestBody ReportQueryUpsertRequest request
    ) {
        return adminService.upsertQuery(cdRelatorio, request);
    }

    @PutMapping("/{cdRelatorio}/filters")
    public ReportAdminDetailDTO upsertFilter(
            @PathVariable String cdRelatorio,
            @Valid @RequestBody ReportFilterUpsertRequest request
    ) {
        return adminService.upsertFilter(cdRelatorio, request);
    }

    @PostMapping(value = "/{cdRelatorio}/templates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReportAdminDetailDTO uploadTemplate(
            @PathVariable String cdRelatorio,
            @RequestPart("file") MultipartFile file,
            @RequestParam(name = "flAtivar", defaultValue = "true") boolean flAtivar
    ) {
        return adminService.uploadTemplate(cdRelatorio, file, flAtivar);
    }

    @PutMapping("/{cdRelatorio}/templates/{idTemplate}/activate")
    public ReportAdminDetailDTO activateTemplate(
            @PathVariable String cdRelatorio,
            @PathVariable Long idTemplate
    ) {
        return adminService.activateTemplate(cdRelatorio, idTemplate);
    }

    @DeleteMapping("/{cdRelatorio}/templates/{idTemplate}")
    public ReportAdminDetailDTO deleteTemplate(
            @PathVariable String cdRelatorio,
            @PathVariable Long idTemplate
    ) {
        return adminService.deleteTemplate(cdRelatorio, idTemplate);
    }
}
