INSERT INTO relatoriotemplate (id_relatorio, nu_versao, nm_arquivo, ds_caminho, fl_ativo)
SELECT id_relatorio, 1, 'v1.jrxml', 'exemplo-visitas/v1.jrxml', TRUE
FROM relatorio
WHERE cd_relatorio = 'exemplo-visitas';
