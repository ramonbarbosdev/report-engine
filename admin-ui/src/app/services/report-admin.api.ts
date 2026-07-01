import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { ReportDetail, ReportSummary, ReportUpsert } from '../models/report.models';

@Injectable({ providedIn: 'root' })
export class ReportAdminApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/reports`;

  list() {
    return this.http.get<ReportSummary[]>(this.baseUrl);
  }

  get(cdRelatorio: string) {
    return this.http.get<ReportDetail>(`${this.baseUrl}/${cdRelatorio}`);
  }

  create(payload: ReportUpsert) {
    return this.http.post<ReportDetail>(this.baseUrl, payload);
  }

  update(cdRelatorio: string, payload: ReportUpsert) {
    return this.http.put<ReportDetail>(`${this.baseUrl}/${cdRelatorio}`, payload);
  }

  upsertQuery(cdRelatorio: string, payload: { nmQuery: string; dsSql: string; flAtivo: boolean }) {
    return this.http.put<ReportDetail>(`${this.baseUrl}/${cdRelatorio}/queries`, payload);
  }

  upsertFilter(
    cdRelatorio: string,
    payload: {
      nmChave: string;
      dsRotulo: string;
      tpFiltro: string;
      flObrigatorio: boolean;
      dsValorPadrao?: string;
      dsQueryOpcoes?: string;
      nuOrdem: number;
    }
  ) {
    return this.http.put<ReportDetail>(`${this.baseUrl}/${cdRelatorio}/filters`, payload);
  }

  uploadTemplate(cdRelatorio: string, file: File, flAtivar = true) {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<ReportDetail>(
      `${this.baseUrl}/${cdRelatorio}/templates?flAtivar=${flAtivar}`,
      form
    );
  }

  activateTemplate(cdRelatorio: string, idTemplate: number) {
    return this.http.put<ReportDetail>(
      `${this.baseUrl}/${cdRelatorio}/templates/${idTemplate}/activate`,
      {}
    );
  }

  deleteTemplate(cdRelatorio: string, idTemplate: number) {
    return this.http.delete<ReportDetail>(
      `${this.baseUrl}/${cdRelatorio}/templates/${idTemplate}`
    );
  }
}
