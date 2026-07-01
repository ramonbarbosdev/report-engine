package br.com.reportengine.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "relatorioquery")
public class ReportQueryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_relatorioquery")
    private Long idRelatorioquery;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_relatorio", nullable = false)
    private ReportDefinitionEntity relatorio;

    @Column(name = "nm_query", nullable = false, length = 100)
    private String nmQuery = "main";

    @Column(name = "tp_query", nullable = false, length = 20)
    private String tpQuery = "SQL";

    @Column(name = "ds_sql", nullable = false, columnDefinition = "TEXT")
    private String dsSql;

    @Column(name = "fl_ativo", nullable = false)
    private boolean flAtivo = true;

    @Column(name = "dt_cadastro", nullable = false)
    private OffsetDateTime dtCadastro = OffsetDateTime.now();

    @Column(name = "dt_alteracao", nullable = false)
    private OffsetDateTime dtAlteracao = OffsetDateTime.now();

    @PreUpdate
    public void onUpdate() {
        dtAlteracao = OffsetDateTime.now();
    }
}
