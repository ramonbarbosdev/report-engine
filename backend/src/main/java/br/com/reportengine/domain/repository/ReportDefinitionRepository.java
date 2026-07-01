package br.com.reportengine.domain.repository;

import br.com.reportengine.domain.entity.ReportDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReportDefinitionRepository extends JpaRepository<ReportDefinitionEntity, Long> {

    List<ReportDefinitionEntity> findByFlAtivoTrueOrderByNmRelatorioAsc();

    Optional<ReportDefinitionEntity> findByCdRelatorio(String cdRelatorio);

    boolean existsByCdRelatorio(String cdRelatorio);
}
