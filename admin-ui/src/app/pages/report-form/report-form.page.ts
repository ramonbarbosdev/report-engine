import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReportAdminApi } from '../../services/report-admin.api';
import { ReportDetail, ReportFilter, ReportQuery, ReportTemplate } from '../../models/report.models';
import { SqlEditorComponent } from '../../shared/components/sql-editor/sql-editor.component';
import { AlertBannerComponent } from '../../shared/components/alert-banner/alert-banner.component';

export type ReportFormTab = 'geral' | 'queries' | 'filtros' | 'templates';

@Component({
  selector: 'app-report-form-page',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterLink,
    SqlEditorComponent,
    AlertBannerComponent,
  ],
  templateUrl: './report-form.page.html',
})
export class ReportFormPage implements OnInit {
  private readonly api = inject(ReportAdminApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  isNew = true;
  cdRelatorio = '';
  detail: ReportDetail | null = null;
  loading = false;
  saving = false;
  savingQuery = false;
  savingFilter = false;
  uploadingTemplate = false;
  message = '';
  error = '';
  queryErrors: string[] = [];
  queryWarnings: string[] = [];
  validatingQuery = false;
  activeTab: ReportFormTab = 'geral';
  selectedQueryName = '';
  selectedFilterKey = '';

  reportForm = this.fb.group({
    cdRelatorio: ['', [Validators.required]],
    nmRelatorio: ['', [Validators.required]],
    nmCategoria: [''],
    dsRelatorio: [''],
    nmDatasource: ['principal', [Validators.required]],
    dsFormatosSaida: ['PDF,XLSX'],
    flAtivo: [true],
  });

  queryForm = this.fb.group({
    nmQuery: ['main', [Validators.required]],
    dsSql: ['', [Validators.required]],
    flAtivo: [true],
  });

  filterForm = this.fb.group({
    nmChave: ['', [Validators.required]],
    dsRotulo: ['', [Validators.required]],
    tpFiltro: ['DATE', [Validators.required]],
    flObrigatorio: [false],
    dsValorPadrao: [''],
    dsQueryOpcoes: [''],
    nuOrdem: [0],
  });

  selectedTemplateFile: File | null = null;
  selectedTemplateFileName = '';
  activateOnUpload = true;
  templateActionId: number | null = null;

  ngOnInit(): void {
    this.cdRelatorio = this.route.snapshot.paramMap.get('code') ?? '';
    this.isNew = this.cdRelatorio === 'new' || !this.cdRelatorio;

    if (!this.isNew) {
      this.loading = true;
      this.api.get(this.cdRelatorio).subscribe({
        next: (detail) => {
          this.patchDetail(detail);
          this.loading = false;
        },
        error: (err) => {
          this.error = err?.error?.detail ?? 'Falha ao carregar relatório';
          this.loading = false;
        },
      });
    }
  }

  setTab(tab: ReportFormTab): void {
    if (this.isNew && tab !== 'geral') {
      return;
    }
    this.activeTab = tab;
  }

  saveReport(): void {
    if (this.reportForm.invalid) {
      return;
    }

    this.saving = true;
    this.clearAlerts();
    const payload = this.reportForm.getRawValue();
    const request$ = this.isNew
      ? this.api.create(payload as any)
      : this.api.update(this.cdRelatorio, payload as any);

    request$.subscribe({
      next: (detail) => {
        this.saving = false;
        this.message = 'Relatório salvo com sucesso';
        if (this.isNew) {
          this.router.navigate(['/reports', detail.cdRelatorio]);
        } else {
          this.patchDetail(detail);
        }
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.detail ?? 'Falha ao salvar relatório';
      },
    });
  }

  saveQuery(): void {
    if (this.queryForm.invalid || this.isNew) {
      return;
    }

    this.savingQuery = true;
    this.clearAlerts();
    this.api.upsertQuery(this.cdRelatorio, this.queryForm.getRawValue() as any).subscribe({
      next: (result) => {
        this.savingQuery = false;
        this.message = 'Query salva com sucesso';
        this.queryWarnings = result.warnings ?? [];
        this.patchDetail(result.detail);
        const nmQuery = this.queryForm.getRawValue().nmQuery!;
        this.selectQuery(nmQuery);
      },
      error: (err) => this.handleQueryError(err, 'Falha ao salvar query', () => {
        this.savingQuery = false;
      }),
    });
  }

  validateQuery(): void {
    if (this.queryForm.invalid || this.isNew) {
      return;
    }

    this.validatingQuery = true;
    this.clearQueryValidation();
    this.api.validateQuery(this.cdRelatorio, this.queryForm.getRawValue() as any).subscribe({
      next: (result) => {
        this.validatingQuery = false;
        this.queryErrors = result.errors ?? [];
        this.queryWarnings = result.warnings ?? [];
        if (result.valid && !this.queryWarnings.length) {
          this.message = 'SQL valido. Nenhum problema encontrado.';
        } else if (result.valid) {
          this.message = 'SQL valido, mas ha avisos. Revise antes de gerar o relatorio.';
        }
      },
      error: (err) => this.handleQueryError(err, 'Falha ao validar query', () => {
        this.validatingQuery = false;
      }),
    });
  }

  selectQuery(nmQuery: string): void {
    const query = this.detail?.queries.find((q) => q.nmQuery === nmQuery);
    if (!query) {
      return;
    }
    this.selectedQueryName = nmQuery;
    this.clearQueryValidation();
    this.queryForm.patchValue({
      nmQuery: query.nmQuery,
      dsSql: query.dsSql,
      flAtivo: query.flAtivo,
    });
  }

  newQuery(): void {
    this.selectedQueryName = '';
    this.clearQueryValidation();
    this.queryForm.reset({
      nmQuery: '',
      dsSql: '',
      flAtivo: true,
    });
  }

  saveFilter(): void {
    if (this.filterForm.invalid || this.isNew) {
      return;
    }

    this.savingFilter = true;
    this.clearAlerts();
    this.api.upsertFilter(this.cdRelatorio, this.filterForm.getRawValue() as any).subscribe({
      next: (detail) => {
        this.savingFilter = false;
        this.message = 'Filtro salvo com sucesso';
        this.patchDetail(detail);
        this.newFilter();
      },
      error: (err) => {
        this.savingFilter = false;
        this.error = err?.error?.detail ?? 'Falha ao salvar filtro';
      },
    });
  }

  selectFilter(filtro: ReportFilter): void {
    this.selectedFilterKey = filtro.nmChave;
    this.filterForm.patchValue({
      nmChave: filtro.nmChave,
      dsRotulo: filtro.dsRotulo,
      tpFiltro: filtro.tpFiltro,
      flObrigatorio: filtro.flObrigatorio,
      dsValorPadrao: filtro.dsValorPadrao ?? '',
      dsQueryOpcoes: filtro.dsQueryOpcoes ?? '',
      nuOrdem: filtro.nuOrdem,
    });
  }

  newFilter(): void {
    this.selectedFilterKey = '';
    this.filterForm.reset({
      nmChave: '',
      dsRotulo: '',
      tpFiltro: 'DATE',
      flObrigatorio: false,
      dsValorPadrao: '',
      dsQueryOpcoes: '',
      nuOrdem: (this.detail?.filtros.length ?? 0) + 1,
    });
  }

  onTemplateSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedTemplateFile = input.files?.[0] ?? null;
    this.selectedTemplateFileName = this.selectedTemplateFile?.name ?? '';
  }

  uploadTemplate(): void {
    if (!this.selectedTemplateFile || this.isNew) {
      return;
    }

    this.uploadingTemplate = true;
    this.clearAlerts();
    this.api.uploadTemplate(this.cdRelatorio, this.selectedTemplateFile, this.activateOnUpload).subscribe({
      next: (detail) => {
        this.uploadingTemplate = false;
        this.message = this.activateOnUpload
          ? 'Template enviado e ativado com sucesso'
          : 'Template enviado (versão inativa). Ative quando estiver pronto.';
        this.patchDetail(detail);
        this.selectedTemplateFile = null;
        this.selectedTemplateFileName = '';
      },
      error: (err) => {
        this.uploadingTemplate = false;
        this.error = err?.error?.detail ?? 'Falha ao enviar template';
      },
    });
  }

  activateTemplate(idTemplate: number): void {
    if (this.isNew) {
      return;
    }
    this.templateActionId = idTemplate;
    this.clearAlerts();
    this.api.activateTemplate(this.cdRelatorio, idTemplate).subscribe({
      next: (detail) => {
        this.templateActionId = null;
        this.message = 'Template ativado com sucesso';
        this.patchDetail(detail);
      },
      error: (err) => {
        this.templateActionId = null;
        this.error = err?.error?.detail ?? 'Falha ao ativar template';
      },
    });
  }

  deleteTemplate(idTemplate: number, nmArquivo: string, nuVersao: number): void {
    if (this.isNew) {
      return;
    }
    const ok = confirm(
      `Excluir a versão v${nuVersao} (${nmArquivo})?\n\nEsta ação não pode ser desfeita.`
    );
    if (!ok) {
      return;
    }
    this.templateActionId = idTemplate;
    this.clearAlerts();
    this.api.deleteTemplate(this.cdRelatorio, idTemplate).subscribe({
      next: (detail) => {
        this.templateActionId = null;
        this.message = 'Template excluído com sucesso';
        this.patchDetail(detail);
      },
      error: (err) => {
        this.templateActionId = null;
        this.error = err?.error?.detail ?? 'Falha ao excluir template';
      },
    });
  }

  get sortedQueries(): ReportQuery[] {
    return [...(this.detail?.queries ?? [])].sort((a, b) => a.nmQuery.localeCompare(b.nmQuery));
  }

  get sortedTemplates(): ReportTemplate[] {
    return [...(this.detail?.templates ?? [])].sort((a, b) => b.nuVersao - a.nuVersao);
  }

  get sortedFilters(): ReportFilter[] {
    return [...(this.detail?.filtros ?? [])].sort((a, b) => a.nuOrdem - b.nuOrdem);
  }

  isSelectFilter(): boolean {
    return this.filterForm.getRawValue().tpFiltro === 'SELECT';
  }

  private patchDetail(detail: ReportDetail): void {
    this.detail = detail;
    this.reportForm.patchValue({
      cdRelatorio: detail.cdRelatorio,
      nmRelatorio: detail.nmRelatorio,
      nmCategoria: detail.nmCategoria ?? '',
      dsRelatorio: detail.dsRelatorio ?? '',
      nmDatasource: detail.nmDatasource,
      dsFormatosSaida: detail.dsFormatosSaida,
      flAtivo: detail.flAtivo,
    });

    const mainQuery = detail.queries.find((q) => q.nmQuery === 'main');
    if (mainQuery) {
      this.selectQuery(mainQuery.nmQuery);
    } else if (detail.queries.length > 0) {
      this.selectQuery(detail.queries[0].nmQuery);
    }
  }

  private   clearAlerts(): void {
    this.message = '';
    this.error = '';
    this.clearQueryValidation();
  }

  clearQueryValidation(): void {
    this.queryErrors = [];
    this.queryWarnings = [];
  }

  private handleQueryError(err: any, fallback: string, finalize?: () => void): void {
    finalize?.();
    const body = err?.error;
    if (Array.isArray(body?.errors) && body.errors.length) {
      this.queryErrors = body.errors;
    }
    if (Array.isArray(body?.warnings) && body.warnings.length) {
      this.queryWarnings = body.warnings;
    }
    this.error = body?.detail ?? fallback;
  }
}
