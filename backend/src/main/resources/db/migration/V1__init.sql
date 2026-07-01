CREATE TABLE relatorio (
    id_relatorio       BIGSERIAL PRIMARY KEY,
    cd_relatorio       VARCHAR(100) NOT NULL UNIQUE,
    nm_relatorio       VARCHAR(200) NOT NULL,
    nm_categoria       VARCHAR(100),
    ds_relatorio       TEXT,
    nm_datasource      VARCHAR(50) NOT NULL DEFAULT 'principal',
    ds_formatos_saida  VARCHAR(100) NOT NULL DEFAULT 'PDF,XLSX',
    fl_ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    nu_versao          INTEGER NOT NULL DEFAULT 1,
    dt_cadastro        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    dt_alteracao       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE relatoriotemplate (
    id_relatoriotemplate BIGSERIAL PRIMARY KEY,
    id_relatorio         BIGINT NOT NULL REFERENCES relatorio(id_relatorio) ON DELETE CASCADE,
    nu_versao            INTEGER NOT NULL,
    nm_arquivo           VARCHAR(255) NOT NULL,
    ds_caminho           VARCHAR(500) NOT NULL,
    fl_ativo             BOOLEAN NOT NULL DEFAULT FALSE,
    dt_cadastro          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (id_relatorio, nu_versao)
);

CREATE TABLE relatorioquery (
    id_relatorioquery BIGSERIAL PRIMARY KEY,
    id_relatorio      BIGINT NOT NULL REFERENCES relatorio(id_relatorio) ON DELETE CASCADE,
    nm_query          VARCHAR(100) NOT NULL DEFAULT 'main',
    tp_query          VARCHAR(20) NOT NULL DEFAULT 'SQL',
    ds_sql            TEXT NOT NULL,
    fl_ativo          BOOLEAN NOT NULL DEFAULT TRUE,
    dt_cadastro       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    dt_alteracao      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (id_relatorio, nm_query)
);

CREATE TABLE relatoriofiltro (
    id_relatoriofiltro BIGSERIAL PRIMARY KEY,
    id_relatorio       BIGINT NOT NULL REFERENCES relatorio(id_relatorio) ON DELETE CASCADE,
    nm_chave           VARCHAR(100) NOT NULL,
    ds_rotulo          VARCHAR(200) NOT NULL,
    tp_filtro          VARCHAR(30) NOT NULL,
    fl_obrigatorio     BOOLEAN NOT NULL DEFAULT FALSE,
    ds_valor_padrao    VARCHAR(500),
    ds_query_opcoes    TEXT,
    nu_ordem           INTEGER NOT NULL DEFAULT 0,
    dt_cadastro        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (id_relatorio, nm_chave)
);

CREATE TABLE relatorioexecucao (
    id_relatorioexecucao BIGSERIAL PRIMARY KEY,
    id_relatorio         BIGINT REFERENCES relatorio(id_relatorio) ON DELETE SET NULL,
    cd_relatorio         VARCHAR(100) NOT NULL,
    tp_formato_saida     VARCHAR(10) NOT NULL,
    ds_filtros_json      TEXT,
    nu_linhas            INTEGER,
    nu_duracao_ms        BIGINT,
    fl_sucesso           BOOLEAN NOT NULL,
    ds_erro              TEXT,
    nm_solicitante       VARCHAR(150),
    dt_cadastro          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_relatorio_fl_ativo ON relatorio(fl_ativo);
CREATE INDEX idx_relatorioexecucao_cd_relatorio ON relatorioexecucao(cd_relatorio);
CREATE INDEX idx_relatorioexecucao_dt_cadastro ON relatorioexecucao(dt_cadastro DESC);
