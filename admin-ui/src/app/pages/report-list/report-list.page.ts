import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ReportAdminApi } from '../../services/report-admin.api';
import { ReportSummary } from '../../models/report.models';

@Component({
  selector: 'app-report-list-page',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './report-list.page.html',
})
export class ReportListPage implements OnInit {
  private readonly api = inject(ReportAdminApi);

  reports: ReportSummary[] = [];
  filtered: ReportSummary[] = [];
  loading = true;
  error = '';
  search = '';

  ngOnInit(): void {
    this.api.list().subscribe({
      next: (data) => {
        this.reports = data;
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.detail ?? 'Falha ao carregar relatórios';
        this.loading = false;
      },
    });
  }

  onSearchChange(): void {
    this.applyFilter();
  }

  private applyFilter(): void {
    const term = this.search.trim().toLowerCase();
    if (!term) {
      this.filtered = [...this.reports];
      return;
    }
    this.filtered = this.reports.filter(
      (r) =>
        r.cdRelatorio.toLowerCase().includes(term) ||
        r.nmRelatorio.toLowerCase().includes(term) ||
        (r.nmCategoria ?? '').toLowerCase().includes(term)
    );
  }
}
