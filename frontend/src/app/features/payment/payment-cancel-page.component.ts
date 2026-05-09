import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-payment-cancel-page',
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule, MatCardModule],
  template: `
    <section class="state-page">
      <mat-card appearance="outlined" class="state-card">
        <p class="state-card__eyebrow">Payment canceled</p>
        <h1>Placanje je prekinuto</h1>
        <p>
          Nije izvrsena naplata. Mozes se vratiti na izbor sedišta i pokusati ponovo.
        </p>
        <div class="state-card__actions">
          <a mat-raised-button color="primary" routerLink="/tickets">Return to tickets</a>
          <a mat-stroked-button routerLink="/checkout">Open checkout</a>
        </div>
      </mat-card>
    </section>
  `,
  styles: [
    `
      .state-page {
        min-height: 58vh;
        display: grid;
        place-items: center;
      }

      .state-card {
        max-width: 720px;
        border-radius: 20px;
        border: 1px solid rgba(248, 113, 113, 0.35);
        background: rgba(15, 23, 42, 0.58);
      }

      .state-card__eyebrow {
        margin: 0;
        font-size: 0.72rem;
        text-transform: uppercase;
        letter-spacing: 0.14em;
        color: #fca5a5;
      }

      h1 {
        margin: 0.45rem 0;
        color: #f8fafc;
      }

      p {
        color: #cbd5e1;
      }

      .state-card__actions {
        margin-top: 1rem;
        display: flex;
        gap: 0.65rem;
        flex-wrap: wrap;
      }
    `
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PaymentCancelPageComponent {}
