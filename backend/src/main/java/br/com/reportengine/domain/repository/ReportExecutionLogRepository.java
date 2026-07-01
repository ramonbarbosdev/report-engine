package br.com.reportengine.domain.repository;

import br.com.reportengine.domain.entity.ReportExecutionLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportExecutionLogRepository extends JpaRepository<ReportExecutionLogEntity, Long> {
}
