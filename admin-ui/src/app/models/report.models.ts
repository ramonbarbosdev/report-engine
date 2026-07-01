export interface ReportSummary {
  idRelatorio: number;
  cdRelatorio: string;
  nmRelatorio: string;
  nmCategoria: string | null;
  flAtivo: boolean;
  nuVersao: number;
}

export interface ReportFilter {
  idRelatoriofiltro?: number;
  nmChave: string;
  dsRotulo: string;
  tpFiltro: string;
  flObrigatorio: boolean;
  dsValorPadrao?: string | null;
  dsQueryOpcoes?: string | null;
  nuOrdem: number;
}

export interface ReportQuery {
  idRelatorioquery?: number;
  nmQuery: string;
  dsSql: string;
  flAtivo: boolean;
}

export interface ReportTemplate {
  idRelatoriotemplate: number;
  nuVersao: number;
  nmArquivo: string;
  flAtivo: boolean;
  dtCadastro: string;
}

export interface ReportDetail {
  idRelatorio: number;
  cdRelatorio: string;
  nmRelatorio: string;
  nmCategoria: string | null;
  dsRelatorio: string | null;
  nmDatasource: string;
  dsFormatosSaida: string;
  flAtivo: boolean;
  nuVersao: number;
  dtAlteracao: string;
  filtros: ReportFilter[];
  queries: ReportQuery[];
  templates: ReportTemplate[];
}

export interface ReportUpsert {
  cdRelatorio: string;
  nmRelatorio: string;
  nmCategoria?: string;
  dsRelatorio?: string;
  nmDatasource: string;
  flAtivo: boolean;
  dsFormatosSaida?: string;
}

export interface ReportQueryValidation {
  valid: boolean;
  errors: string[];
  warnings: string[];
}

export interface ReportQuerySaveResult {
  detail: ReportDetail;
  warnings: string[];
}
