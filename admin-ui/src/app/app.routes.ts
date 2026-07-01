import { Routes } from '@angular/router';
import { ReportListPage } from './pages/report-list/report-list.page';
import { ReportFormPage } from './pages/report-form/report-form.page';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'reports' },
  { path: 'reports', component: ReportListPage },
  { path: 'reports/new', component: ReportFormPage },
  { path: 'reports/:code', component: ReportFormPage },
];
