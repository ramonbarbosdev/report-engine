import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReportAdminApi } from '../../services/report-admin.api';
import { ReportSummary } from '../../models/report.models';

@Component({
  selector: 'app-report-list-page',
  imports: [CommonModule, RouterLink],
  templateUrl: './report-list.page.html',
})
export class ReportListPage implements OnInit {
  private readonly api = inject(ReportAdminApi);

  reports: ReportSummary[] = [];
  loading = true;
  error = '';

  ngOnInit(): void {
    this.api.list().subscribe({
      next: (data) => {
        this.reports = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.detail ?? 'Falha ao carregar relatorios';
        this.loading = false;
      },
    });
  }
}
