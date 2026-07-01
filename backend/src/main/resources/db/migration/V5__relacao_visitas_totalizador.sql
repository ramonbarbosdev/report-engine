INSERT INTO relatorioquery (id_relatorio, nm_query, tp_query, ds_sql, fl_ativo)
SELECT id_relatorio, 'totalizador', 'SQL',
$$
SELECT
    COUNT(*) AS quantidade,
    COALESCE(SUM(av.vl_visitafinal), 0) AS total_visita,
    COALESCE(SUM(av.vl_adicionalvisita), 0) AS total_adicional,
    COALESCE(SUM(av.vl_visitafinal) / NULLIF(COUNT(*), 0), 0) AS valor_medio,
    COALESCE(SUM(av.vl_adicionalvisita) / NULLIF(COUNT(*), 0), 0) AS valor_medioadicional
FROM area_visita av
INNER JOIN projeto p ON av.id_projeto = p.id_projeto
INNER JOIN projeto_credito_rural pcr ON p.id_projeto = pcr.id_projeto
INNER JOIN tecnico t ON t.id_tecnico = pcr.id_tecnico
WHERE av.tp_statusvisita = 'Concluída'
  AND av.dt_visita >= COALESCE(:dataInicial, av.dt_visita)
  AND av.dt_visita <= COALESCE(:dataFinal, av.dt_visita)
  AND p.id_projeto = COALESCE(:idProjeto, p.id_projeto)
  AND t.id_tecnico = COALESCE(:idTecnico, t.id_tecnico)
$$,
    TRUE
FROM relatorio
WHERE cd_relatorio = 'relacao-visitas'
  AND NOT EXISTS (
      SELECT 1 FROM relatorioquery rq
      INNER JOIN relatorio r ON r.id_relatorio = rq.id_relatorio
      WHERE r.cd_relatorio = 'relacao-visitas' AND rq.nm_query = 'totalizador'
  );
