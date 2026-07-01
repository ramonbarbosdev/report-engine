UPDATE relatorioquery
SET ds_sql = $$
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
$$
WHERE id_relatorio = (SELECT id_relatorio FROM relatorio WHERE cd_relatorio = 'relacao-visitas')
  AND nm_query = 'main';
