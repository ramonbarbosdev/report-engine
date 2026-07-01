import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-alert-banner',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (message) {
      <div class="alert" [class]="'alert-' + type" role="alert">
        <span class="alert-icon">{{ icon }}</span>
        <span class="alert-text">{{ message }}</span>
        @if (dismissible) {
          <button type="button" class="alert-close" (click)="message = ''" aria-label="Fechar">×</button>
        }
      </div>
    }
  `,
})
export class AlertBannerComponent {
  @Input() type: 'success' | 'error' | 'info' = 'info';
  @Input() dismissible = true;

  private _message = '';

  @Input()
  set message(value: string) {
    this._message = value;
  }
  get message(): string {
    return this._message;
  }

  get icon(): string {
    switch (this.type) {
      case 'success':
        return '✓';
      case 'error':
        return '!';
      default:
        return 'i';
    }
  }
}
