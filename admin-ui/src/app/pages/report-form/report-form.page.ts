import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReportAdminApi } from '../../services/report-admin.api';
import { ReportDetail, ReportQuery } from '../../models/report.models';

@Component({
  selector: 'app-report-form-page',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
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
  message = '';
  error = '';

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

  ngOnInit(): void {
    this.cdRelatorio = this.route.snapshot.paramMap.get('code') ?? '';
    this.isNew = this.cdRelatorio === 'new' || !this.cdRelatorio;

    if (!this.isNew) {
      this.loading = true;
      this.api.get(this.cdRelatorio).subscribe({
        next: (detail) => {
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
            this.editQuery(mainQuery);
          }
          this.loading = false;
        },
        error: (err) => {
          this.error = err?.error?.detail ?? 'Falha ao carregar relatorio';
          this.loading = false;
        },
      });
    }
  }

  saveReport(): void {
    if (this.reportForm.invalid) {
      return;
    }

    this.saving = true;
    this.error = '';
    const payload = this.reportForm.getRawValue();
    const request$ = this.isNew
      ? this.api.create(payload as any)
      : this.api.update(this.cdRelatorio, payload as any);

    request$.subscribe({
      next: (detail) => {
        this.saving = false;
        this.message = 'Relatorio salvo com sucesso';
        if (this.isNew) {
          this.router.navigate(['/reports', detail.cdRelatorio]);
        } else {
          this.detail = detail;
        }
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.detail ?? 'Falha ao salvar relatorio';
      },
    });
  }

  saveQuery(): void {
    if (this.queryForm.invalid || this.isNew) {
      return;
    }

    this.api.upsertQuery(this.cdRelatorio, this.queryForm.getRawValue() as any).subscribe({
      next: (detail) => {
        this.detail = detail;
        this.message = 'Query salva com sucesso';
        const saved = detail.queries.find(
          (q) => q.nmQuery === this.queryForm.getRawValue().nmQuery
        );
        if (saved) {
          this.editQuery(saved);
        }
      },
      error: (err) => {
        this.error = err?.error?.detail ?? 'Falha ao salvar query';
      },
    });
  }

  editQuery(query: ReportQuery): void {
    this.queryForm.patchValue({
      nmQuery: query.nmQuery,
      dsSql: query.dsSql,
      flAtivo: query.flAtivo,
    });
  }

  newQuery(): void {
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

    this.api.upsertFilter(this.cdRelatorio, this.filterForm.getRawValue() as any).subscribe({
      next: (detail) => {
        this.detail = detail;
        this.message = 'Filtro salvo com sucesso';
        this.filterForm.reset({
          tpFiltro: 'DATE',
          flObrigatorio: false,
          nuOrdem: 0,
        });
      },
      error: (err) => {
        this.error = err?.error?.detail ?? 'Falha ao salvar filtro';
      },
    });
  }

  onTemplateSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedTemplateFile = input.files?.[0] ?? null;
  }

  uploadTemplate(): void {
    if (!this.selectedTemplateFile || this.isNew) {
      return;
    }

    this.api.uploadTemplate(this.cdRelatorio, this.selectedTemplateFile, true).subscribe({
      next: (detail) => {
        this.detail = detail;
        this.message = 'Template enviado com sucesso';
        this.selectedTemplateFile = null;
      },
      error: (err) => {
        this.error = err?.error?.detail ?? 'Falha ao enviar template';
      },
    });
  }
}
