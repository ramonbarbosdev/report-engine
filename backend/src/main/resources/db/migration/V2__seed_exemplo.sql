INSERT INTO relatorio (
    cd_relatorio,
    nm_relatorio,
    nm_categoria,
    ds_relatorio,
    nm_datasource,
    ds_formatos_saida,
    fl_ativo,
    nu_versao
)
VALUES (
    'exemplo-visitas',
    'Relacao de Visitas (exemplo)',
    'geral',
    'Relatorio de exemplo para validar o motor Jasper',
    'principal',
    'PDF,XLSX',
    TRUE,
    1
);

INSERT INTO relatorioquery (id_relatorio, nm_query, tp_query, ds_sql, fl_ativo)
SELECT id_relatorio, 'main', 'SQL',
       'SELECT 1 AS id, ''Exemplo'' AS titulo, CURRENT_DATE AS data_referencia',
       TRUE
FROM relatorio
WHERE cd_relatorio = 'exemplo-visitas';

INSERT INTO relatoriofiltro (id_relatorio, nm_chave, ds_rotulo, tp_filtro, fl_obrigatorio, nu_ordem)
SELECT id_relatorio, 'dataInicio', 'Data inicio', 'DATE', TRUE, 1
FROM relatorio
WHERE cd_relatorio = 'exemplo-visitas';

INSERT INTO relatoriofiltro (id_relatorio, nm_chave, ds_rotulo, tp_filtro, fl_obrigatorio, nu_ordem)
SELECT id_relatorio, 'dataFim', 'Data fim', 'DATE', TRUE, 2
FROM relatorio
WHERE cd_relatorio = 'exemplo-visitas';
