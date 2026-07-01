package br.com.reportengine.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "relatorio")
public class ReportDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_relatorio")
    private Long idRelatorio;

    @Column(name = "cd_relatorio", nullable = false, unique = true, length = 100)
    private String cdRelatorio;

    @Column(name = "nm_relatorio", nullable = false, length = 200)
    private String nmRelatorio;

    @Column(name = "nm_categoria", length = 100)
    private String nmCategoria;

    @Column(name = "ds_relatorio", columnDefinition = "TEXT")
    private String dsRelatorio;

    @Column(name = "nm_datasource", nullable = false, length = 50)
    private String nmDatasource = "principal";

    @Column(name = "ds_formatos_saida", nullable = false, length = 100)
    private String dsFormatosSaida = "PDF,XLSX";

    @Column(name = "fl_ativo", nullable = false)
    private boolean flAtivo = true;

    @Column(name = "nu_versao", nullable = false)
    private Integer nuVersao = 1;

    @Column(name = "dt_cadastro", nullable = false)
    private OffsetDateTime dtCadastro = OffsetDateTime.now();

    @Column(name = "dt_alteracao", nullable = false)
    private OffsetDateTime dtAlteracao = OffsetDateTime.now();

    @OneToMany(mappedBy = "relatorio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("nuVersao DESC")
    private List<ReportTemplateEntity> templates = new ArrayList<>();

    @OneToMany(mappedBy = "relatorio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportQueryEntity> queries = new ArrayList<>();

    @OneToMany(mappedBy = "relatorio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("nuOrdem ASC")
    private List<ReportFilterEntity> filtros = new ArrayList<>();

    @PreUpdate
    public void onUpdate() {
        dtAlteracao = OffsetDateTime.now();
    }
}
