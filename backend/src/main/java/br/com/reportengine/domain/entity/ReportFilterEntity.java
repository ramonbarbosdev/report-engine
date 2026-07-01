package br.com.reportengine.domain.entity;

import br.com.reportengine.domain.enums.FilterType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "relatoriofiltro")
public class ReportFilterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_relatoriofiltro")
    private Long idRelatoriofiltro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_relatorio", nullable = false)
    private ReportDefinitionEntity relatorio;

    @Column(name = "nm_chave", nullable = false, length = 100)
    private String nmChave;

    @Column(name = "ds_rotulo", nullable = false, length = 200)
    private String dsRotulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_filtro", nullable = false, length = 30)
    private FilterType tpFiltro;

    @Column(name = "fl_obrigatorio", nullable = false)
    private boolean flObrigatorio = false;

    @Column(name = "ds_valor_padrao", length = 500)
    private String dsValorPadrao;

    @Column(name = "ds_query_opcoes", columnDefinition = "TEXT")
    private String dsQueryOpcoes;

    @Column(name = "nu_ordem", nullable = false)
    private Integer nuOrdem = 0;

    @Column(name = "dt_cadastro", nullable = false)
    private OffsetDateTime dtCadastro = OffsetDateTime.now();
}
