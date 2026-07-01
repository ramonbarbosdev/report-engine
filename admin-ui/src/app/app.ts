import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-layout">
      <aside class="sidebar">
        <div class="sidebar-brand">
          <h1>Report Engine</h1>
          <p>Painel administrativo</p>
        </div>
        <nav class="sidebar-nav">
          <a routerLink="/reports" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">
            <span></span> Relatórios
          </a>
          <a routerLink="/reports/new" routerLinkActive="active">
            <span></span> Novo relatório
          </a>
        </nav>
      </aside>
      <main class="main-content">
        <router-outlet />
      </main>
    </div>
  `,
})
export class App {}
