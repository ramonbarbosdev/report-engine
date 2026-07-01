import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="topbar">
      <h1>Report Engine Admin</h1>
    </header>
    <nav class="container" style="padding-bottom: 0">
      <a routerLink="/reports" routerLinkActive="active">Relatorios</a>
      &nbsp;|&nbsp;
      <a routerLink="/reports/new" routerLinkActive="active">Novo relatorio</a>
    </nav>
    <main class="container">
      <router-outlet />
    </main>
  `,
})
export class App {}
