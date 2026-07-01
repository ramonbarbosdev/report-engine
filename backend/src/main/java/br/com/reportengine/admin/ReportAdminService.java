package br.com.reportengine.admin;

import br.com.reportengine.admin.dto.*;
import br.com.reportengine.core.JasperReportService;
import br.com.reportengine.core.ReportEngineException;
import br.com.reportengine.core.ReportTemplateStorageService;
import br.com.reportengine.domain.entity.*;
import br.com.reportengine.domain.repository.ReportDefinitionRepository;
import br.com.reportengine.domain.service.ReportDefinitionLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportAdminService {

    private final ReportDefinitionRepository reportRepository;
    private final ReportDefinitionLoader reportDefinitionLoader;
    private final ReportTemplateStorageService templateStorage;
    private final JasperReportService jasperReportService;

    @Transactional(readOnly = true)
    public List<ReportSummaryAdminDTO> listAll() {
        return reportRepository.findAll().stream()
                .sorted(Comparator.comparing(ReportDefinitionEntity::getNmRelatorio))
                .map(r -> new ReportSummaryAdminDTO(
                        r.getIdRelatorio(),
                        r.getCdRelatorio(),
                        r.getNmRelatorio(),
                        r.getNmCategoria(),
                        r.isFlAtivo(),
                        r.getNuVersao()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ReportAdminDetailDTO getByCdRelatorio(String cdRelatorio) {
        return toDetail(findByCdRelatorioOrThrow(cdRelatorio));
    }

    @Transactional
    public ReportAdminDetailDTO create(ReportUpsertRequest request) {
        if (reportRepository.existsByCdRelatorio(request.cdRelatorio())) {
            throw ReportEngineException.conflict("Codigo de relatorio ja existe: " + request.cdRelatorio());
        }
        ReportDefinitionEntity entity = new ReportDefinitionEntity();
        apply(entity, request);
        return toDetail(reportRepository.save(entity));
    }

    @Transactional
    public ReportAdminDetailDTO update(String cdRelatorio, ReportUpsertRequest request) {
        ReportDefinitionEntity entity = findByCdRelatorioOrThrow(cdRelatorio);
        if (!entity.getCdRelatorio().equals(request.cdRelatorio())
                && reportRepository.existsByCdRelatorio(request.cdRelatorio())) {
            throw ReportEngineException.conflict("Codigo de relatorio ja existe: " + request.cdRelatorio());
        }
        apply(entity, request);
        return toDetail(entity);
    }

    @Transactional
    public ReportAdminDetailDTO upsertQuery(String cdRelatorio, ReportQueryUpsertRequest request) {
        ReportDefinitionEntity relatorio = findByCdRelatorioOrThrow(cdRelatorio);
        ReportQueryEntity query = relatorio.getQueries().stream()
                .filter(q -> q.getNmQuery().equalsIgnoreCase(request.nmQuery()))
                .findFirst()
                .orElseGet(() -> {
                    ReportQueryEntity created = new ReportQueryEntity();
                    created.setRelatorio(relatorio);
                    created.setNmQuery(request.nmQuery());
                    relatorio.getQueries().add(created);
                    return created;
                });
        query.setDsSql(request.dsSql());
        query.setFlAtivo(request.flAtivo());
        return toDetail(reportRepository.save(relatorio));
    }

    @Transactional
    public ReportAdminDetailDTO upsertFilter(String cdRelatorio, ReportFilterUpsertRequest request) {
        ReportDefinitionEntity relatorio = findByCdRelatorioOrThrow(cdRelatorio);
        ReportFilterEntity filtro = relatorio.getFiltros().stream()
                .filter(f -> f.getNmChave().equals(request.nmChave()))
                .findFirst()
                .orElseGet(() -> {
                    ReportFilterEntity created = new ReportFilterEntity();
                    created.setRelatorio(relatorio);
                    created.setNmChave(request.nmChave());
                    relatorio.getFiltros().add(created);
                    return created;
                });
        filtro.setDsRotulo(request.dsRotulo());
        filtro.setTpFiltro(request.tpFiltro());
        filtro.setFlObrigatorio(request.flObrigatorio());
        filtro.setDsValorPadrao(request.dsValorPadrao());
        filtro.setDsQueryOpcoes(request.dsQueryOpcoes());
        filtro.setNuOrdem(request.nuOrdem());
        return toDetail(reportRepository.save(relatorio));
    }

    @Transactional
    public ReportAdminDetailDTO uploadTemplate(String cdRelatorio, MultipartFile file, boolean flAtivar) {
        ReportDefinitionEntity relatorio = findByCdRelatorioOrThrow(cdRelatorio);
        int nextVersion = relatorio.getTemplates().stream()
                .mapToInt(ReportTemplateEntity::getNuVersao)
                .max()
                .orElse(0) + 1;

        String dsCaminho = templateStorage.store(cdRelatorio, nextVersion, file);

        if (flAtivar) {
            relatorio.getTemplates().forEach(t -> t.setFlAtivo(false));
        }

        ReportTemplateEntity template = new ReportTemplateEntity();
        template.setRelatorio(relatorio);
        template.setNuVersao(nextVersion);
        template.setNmArquivo(file.getOriginalFilename() == null ? "template.jrxml" : file.getOriginalFilename());
        template.setDsCaminho(dsCaminho);
        template.setFlAtivo(flAtivar);
        relatorio.getTemplates().add(template);
        relatorio.setNuVersao(nextVersion);

        jasperReportService.evictCache(dsCaminho);
        return toDetail(reportRepository.save(relatorio));
    }

    private ReportDefinitionEntity findByCdRelatorioOrThrow(String cdRelatorio) {
        return reportDefinitionLoader.loadWithDetails(cdRelatorio);
    }

    private void apply(ReportDefinitionEntity entity, ReportUpsertRequest request) {
        entity.setCdRelatorio(request.cdRelatorio());
        entity.setNmRelatorio(request.nmRelatorio());
        entity.setNmCategoria(request.nmCategoria());
        entity.setDsRelatorio(request.dsRelatorio());
        entity.setNmDatasource(request.nmDatasource());
        entity.setFlAtivo(request.flAtivo());
        if (request.dsFormatosSaida() != null && !request.dsFormatosSaida().isBlank()) {
            entity.setDsFormatosSaida(request.dsFormatosSaida());
        }
    }

    private ReportAdminDetailDTO toDetail(ReportDefinitionEntity relatorio) {
        return new ReportAdminDetailDTO(
                relatorio.getIdRelatorio(),
                relatorio.getCdRelatorio(),
                relatorio.getNmRelatorio(),
                relatorio.getNmCategoria(),
                relatorio.getDsRelatorio(),
                relatorio.getNmDatasource(),
                relatorio.getDsFormatosSaida(),
                relatorio.isFlAtivo(),
                relatorio.getNuVersao(),
                relatorio.getDtAlteracao(),
                relatorio.getFiltros().stream()
                        .sorted(Comparator.comparing(ReportFilterEntity::getNuOrdem))
                        .map(f -> new ReportFilterAdminDTO(
                                f.getIdRelatoriofiltro(),
                                f.getNmChave(),
                                f.getDsRotulo(),
                                f.getTpFiltro(),
                                f.isFlObrigatorio(),
                                f.getDsValorPadrao(),
                                f.getDsQueryOpcoes(),
                                f.getNuOrdem()
                        ))
                        .toList(),
                relatorio.getQueries().stream()
                        .map(q -> new ReportQueryAdminDTO(
                                q.getIdRelatorioquery(),
                                q.getNmQuery(),
                                q.getDsSql(),
                                q.isFlAtivo()
                        ))
                        .toList(),
                relatorio.getTemplates().stream()
                        .sorted(Comparator.comparing(ReportTemplateEntity::getNuVersao).reversed())
                        .map(t -> new ReportTemplateAdminDTO(
                                t.getIdRelatoriotemplate(),
                                t.getNuVersao(),
                                t.getNmArquivo(),
                                t.isFlAtivo(),
                                t.getDtCadastro()
                        ))
                        .toList()
        );
    }
}
