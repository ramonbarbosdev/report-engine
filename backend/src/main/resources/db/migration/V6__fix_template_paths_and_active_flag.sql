-- Corrige caminho do template v1 do relatorio piloto (arquivo real em v1/area_visitas.jrxml)
UPDATE relatoriotemplate rt
SET ds_caminho = 'relacao-visitas/v1/area_visitas.jrxml',
    nm_arquivo = 'area_visitas.jrxml'
FROM relatorio r
WHERE r.id_relatorio = rt.id_relatorio
  AND r.cd_relatorio = 'relacao-visitas'
  AND rt.nu_versao = 1
  AND rt.ds_caminho = 'relacao-visitas/v1.jrxml';

-- Garante apenas um template ativo por relatorio (mantem a maior versao ativa)
WITH ranked AS (
    SELECT
        id_relatoriotemplate,
        ROW_NUMBER() OVER (
            PARTITION BY id_relatorio
            ORDER BY nu_versao DESC
        ) AS rn
    FROM relatoriotemplate
    WHERE fl_ativo = TRUE
)
UPDATE relatoriotemplate rt
SET fl_ativo = FALSE
FROM ranked
WHERE rt.id_relatoriotemplate = ranked.id_relatoriotemplate
  AND ranked.rn > 1;
