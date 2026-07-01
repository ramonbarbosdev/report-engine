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
    'relacao-visitas',
    'Relacao de Visitas',
    'geral',
    'Relatorio piloto migrado do agrotech (geral/relacaovisita)',
    'principal',
    'PDF,XLSX',
    TRUE,
    1
);

INSERT INTO relatorioquery (id_relatorio, nm_query, tp_query, ds_sql, fl_ativo)
SELECT id_relatorio, 'main', 'SQL',
$$
SELECT DISTINCT
    c.nm_cliente,
    pc.nm_propriedadecliente,
    t.nm_tecnico,
    av.vl_visitafinal,
    av.vl_adicionalvisita,
    av.dt_visita,
    av.tp_statusvisita
FROM area_visita av
INNER JOIN projeto p ON av.id_projeto = p.id_projeto
INNER JOIN projeto_credito_rural pcr ON p.id_projeto = pcr.id_projeto
INNER JOIN propriedade_cliente pc ON pcr.id_propriedadecliente = pc.id_propriedadecliente
INNER JOIN cliente c ON p.id_cliente = c.id_cliente
INNER JOIN tecnico t ON t.id_tecnico = pcr.id_tecnico
WHERE av.tp_statusvisita = 'Concluída'
  AND av.dt_visita >= COALESCE(:dataInicial, av.dt_visita)
  AND av.dt_visita <= COALESCE(:dataFinal, av.dt_visita)
  AND p.id_projeto = COALESCE(:idProjeto, p.id_projeto)
  AND t.id_tecnico = COALESCE(:idTecnico, t.id_tecnico)
ORDER BY av.dt_visita, c.nm_cliente
$$,
    TRUE
FROM relatorio
WHERE cd_relatorio = 'relacao-visitas';

INSERT INTO relatoriofiltro (id_relatorio, nm_chave, ds_rotulo, tp_filtro, fl_obrigatorio, nu_ordem)
SELECT id_relatorio, 'dataInicial', 'Data inicial', 'DATE', FALSE, 1
FROM relatorio WHERE cd_relatorio = 'relacao-visitas';

INSERT INTO relatoriofiltro (id_relatorio, nm_chave, ds_rotulo, tp_filtro, fl_obrigatorio, nu_ordem)
SELECT id_relatorio, 'dataFinal', 'Data final', 'DATE', FALSE, 2
FROM relatorio WHERE cd_relatorio = 'relacao-visitas';

INSERT INTO relatoriofiltro (id_relatorio, nm_chave, ds_rotulo, tp_filtro, fl_obrigatorio, nu_ordem)
SELECT id_relatorio, 'idProjeto', 'Projeto', 'NUMBER', FALSE, 3
FROM relatorio WHERE cd_relatorio = 'relacao-visitas';

INSERT INTO relatoriofiltro (id_relatorio, nm_chave, ds_rotulo, tp_filtro, fl_obrigatorio, nu_ordem)
SELECT id_relatorio, 'idTecnico', 'Tecnico', 'NUMBER', FALSE, 4
FROM relatorio WHERE cd_relatorio = 'relacao-visitas';

INSERT INTO relatoriotemplate (id_relatorio, nu_versao, nm_arquivo, ds_caminho, fl_ativo)
SELECT id_relatorio, 1, 'v1.jrxml', 'relacao-visitas/v1.jrxml', TRUE
FROM relatorio
WHERE cd_relatorio = 'relacao-visitas';
