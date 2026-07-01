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
SELECT
    'comissao-projetista',
    'Comissao do Projetista',
    'geral',
    'Relatorio migrado do agrotech (geral/comissaoprojetista)',
    'principal',
    'PDF,XLSX',
    TRUE,
    1
WHERE NOT EXISTS (
    SELECT 1 FROM relatorio WHERE cd_relatorio = 'comissao-projetista'
);

INSERT INTO relatorioquery (id_relatorio, nm_query, tp_query, ds_sql, fl_ativo)
SELECT id_relatorio, 'main', 'SQL',
$$
SELECT
    p.nm_projeto,
    c.nm_cliente,
    pp.nm_projetista,
    p.nu_percentualprojetista,
    p.vl_comissaoprojetista,
    p.vl_projeto
FROM projeto p
INNER JOIN cliente c ON c.id_cliente = p.id_cliente
INNER JOIN projetista pp ON pp.id_projetista = p.id_projetista
WHERE p.nu_percentualprojetista IS NOT NULL
  AND p.dt_projeto >= COALESCE(:dataInicialProjeto, p.dt_projeto)
  AND p.dt_projeto <= COALESCE(:dataFinalProjeto, p.dt_projeto)
  AND p.id_projetista = COALESCE(:idProjetista, p.id_projetista)
ORDER BY pp.nm_projetista, c.nm_cliente, p.nm_projeto
$$,
    TRUE
FROM relatorio
WHERE cd_relatorio = 'comissao-projetista'
  AND NOT EXISTS (
      SELECT 1 FROM relatorioquery rq
      INNER JOIN relatorio r ON r.id_relatorio = rq.id_relatorio
      WHERE r.cd_relatorio = 'comissao-projetista' AND rq.nm_query = 'main'
  );

INSERT INTO relatorioquery (id_relatorio, nm_query, tp_query, ds_sql, fl_ativo)
SELECT id_relatorio, 'totalizador', 'SQL',
$$
SELECT
    COUNT(*) AS quantidade,
    COALESCE(SUM(p.vl_projeto), 0) AS valor_total,
    COALESCE(SUM(p.vl_comissaoprojetista), 0) AS valor_total_comissao
FROM projeto p
INNER JOIN projetista pp ON pp.id_projetista = p.id_projetista
WHERE p.nu_percentualprojetista IS NOT NULL
  AND p.dt_projeto >= COALESCE(:dataInicialProjeto, p.dt_projeto)
  AND p.dt_projeto <= COALESCE(:dataFinalProjeto, p.dt_projeto)
  AND p.id_projetista = COALESCE(:idProjetista, p.id_projetista)
$$,
    TRUE
FROM relatorio
WHERE cd_relatorio = 'comissao-projetista'
  AND NOT EXISTS (
      SELECT 1 FROM relatorioquery rq
      INNER JOIN relatorio r ON r.id_relatorio = rq.id_relatorio
      WHERE r.cd_relatorio = 'comissao-projetista' AND rq.nm_query = 'totalizador'
  );

INSERT INTO relatoriofiltro (id_relatorio, nm_chave, ds_rotulo, tp_filtro, fl_obrigatorio, nu_ordem)
SELECT id_relatorio, 'dataInicialProjeto', 'Data inicial do projeto', 'DATE', FALSE, 1
FROM relatorio WHERE cd_relatorio = 'comissao-projetista'
  AND NOT EXISTS (
      SELECT 1 FROM relatoriofiltro rf
      INNER JOIN relatorio r ON r.id_relatorio = rf.id_relatorio
      WHERE r.cd_relatorio = 'comissao-projetista' AND rf.nm_chave = 'dataInicialProjeto'
  );

INSERT INTO relatoriofiltro (id_relatorio, nm_chave, ds_rotulo, tp_filtro, fl_obrigatorio, nu_ordem)
SELECT id_relatorio, 'dataFinalProjeto', 'Data final do projeto', 'DATE', FALSE, 2
FROM relatorio WHERE cd_relatorio = 'comissao-projetista'
  AND NOT EXISTS (
      SELECT 1 FROM relatoriofiltro rf
      INNER JOIN relatorio r ON r.id_relatorio = rf.id_relatorio
      WHERE r.cd_relatorio = 'comissao-projetista' AND rf.nm_chave = 'dataFinalProjeto'
  );

INSERT INTO relatoriofiltro (id_relatorio, nm_chave, ds_rotulo, tp_filtro, fl_obrigatorio, nu_ordem)
SELECT id_relatorio, 'idProjetista', 'Projetista', 'NUMBER', FALSE, 3
FROM relatorio WHERE cd_relatorio = 'comissao-projetista'
  AND NOT EXISTS (
      SELECT 1 FROM relatoriofiltro rf
      INNER JOIN relatorio r ON r.id_relatorio = rf.id_relatorio
      WHERE r.cd_relatorio = 'comissao-projetista' AND rf.nm_chave = 'idProjetista'
  );

INSERT INTO relatoriotemplate (id_relatorio, nu_versao, nm_arquivo, ds_caminho, fl_ativo)
SELECT id_relatorio, 1, 'comissao_projetista.jrxml', 'comissao-projetista/v1/comissao_projetista.jrxml', TRUE
FROM relatorio
WHERE cd_relatorio = 'comissao-projetista'
  AND NOT EXISTS (
      SELECT 1 FROM relatoriotemplate rt
      INNER JOIN relatorio r ON r.id_relatorio = rt.id_relatorio
      WHERE r.cd_relatorio = 'comissao-projetista'
  );

UPDATE relatorioquery rq
SET ds_sql = $$
SELECT
    p.nm_projeto,
    c.nm_cliente,
    pp.nm_projetista,
    p.nu_percentualprojetista,
    p.vl_comissaoprojetista,
    p.vl_projeto
FROM projeto p
INNER JOIN cliente c ON c.id_cliente = p.id_cliente
INNER JOIN projetista pp ON pp.id_projetista = p.id_projetista
WHERE p.nu_percentualprojetista IS NOT NULL
  AND p.dt_projeto >= COALESCE(:dataInicialProjeto, p.dt_projeto)
  AND p.dt_projeto <= COALESCE(:dataFinalProjeto, p.dt_projeto)
  AND p.id_projetista = COALESCE(:idProjetista, p.id_projetista)
ORDER BY pp.nm_projetista, c.nm_cliente, p.nm_projeto
$$
FROM relatorio r
WHERE r.id_relatorio = rq.id_relatorio
  AND r.cd_relatorio = 'comissao-projetista'
  AND rq.nm_query = 'main';

UPDATE relatorioquery rq
SET ds_sql = $$
SELECT
    COUNT(*) AS quantidade,
    COALESCE(SUM(p.vl_projeto), 0) AS valor_total,
    COALESCE(SUM(p.vl_comissaoprojetista), 0) AS valor_total_comissao
FROM projeto p
INNER JOIN projetista pp ON pp.id_projetista = p.id_projetista
WHERE p.nu_percentualprojetista IS NOT NULL
  AND p.dt_projeto >= COALESCE(:dataInicialProjeto, p.dt_projeto)
  AND p.dt_projeto <= COALESCE(:dataFinalProjeto, p.dt_projeto)
  AND p.id_projetista = COALESCE(:idProjetista, p.id_projetista)
$$
FROM relatorio r
WHERE r.id_relatorio = rq.id_relatorio
  AND r.cd_relatorio = 'comissao-projetista'
  AND rq.nm_query = 'totalizador';
