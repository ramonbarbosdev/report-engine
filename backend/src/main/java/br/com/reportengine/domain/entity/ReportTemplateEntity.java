package br.com.reportengine.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "relatoriotemplate")
public class ReportTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_relatoriotemplate")
    private Long idRelatoriotemplate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_relatorio", nullable = false)
    private ReportDefinitionEntity relatorio;

    @Column(name = "nu_versao", nullable = false)
    private Integer nuVersao;

    @Column(name = "nm_arquivo", nullable = false)
    private String nmArquivo;

    @Column(name = "ds_caminho", nullable = false, length = 500)
    private String dsCaminho;

    @Column(name = "fl_ativo", nullable = false)
    private boolean flAtivo = false;

    @Column(name = "dt_cadastro", nullable = false)
    private OffsetDateTime dtCadastro = OffsetDateTime.now();
}
