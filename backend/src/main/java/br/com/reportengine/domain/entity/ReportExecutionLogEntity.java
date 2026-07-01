package br.com.reportengine.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "relatorioexecucao")
public class ReportExecutionLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_relatorioexecucao")
    private Long idRelatorioexecucao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_relatorio")
    private ReportDefinitionEntity relatorio;

    @Column(name = "cd_relatorio", nullable = false, length = 100)
    private String cdRelatorio;

    @Column(name = "tp_formato_saida", nullable = false, length = 10)
    private String tpFormatoSaida;

    @Column(name = "ds_filtros_json", columnDefinition = "TEXT")
    private String dsFiltrosJson;

    @Column(name = "nu_linhas")
    private Integer nuLinhas;

    @Column(name = "nu_duracao_ms")
    private Long nuDuracaoMs;

    @Column(name = "fl_sucesso", nullable = false)
    private boolean flSucesso;

    @Column(name = "ds_erro", columnDefinition = "TEXT")
    private String dsErro;

    @Column(name = "nm_solicitante", length = 150)
    private String nmSolicitante;

    @Column(name = "dt_cadastro", nullable = false)
    private OffsetDateTime dtCadastro = OffsetDateTime.now();
}
