import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BookingService } from '../../core/services/booking.service';
import { OrderResponse } from '../../core/models/booking.models';

@Component({
  selector: 'app-payment-success-page',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink, 
    MatButtonModule, 
    MatCardModule],
  templateUrl: './payment-success-page.component.html',
  styleUrls: ['./payment-success-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PaymentSuccessPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly bookingService = inject(BookingService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly order = signal<OrderResponse | null>(null);

  ngOnInit() {
    this.route.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((params) => {
        const orderId = Number(params.get('orderId'));

        if (!Number.isFinite(orderId) || orderId <= 0) {
          this.errorMessage.set('Missing or invalid orderId in success URL.');
          this.isLoading.set(false);
          return;
        }

        this.loadOrder(orderId);
      });
  }

  private loadOrder(orderId: number) {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.order.set(null);

    this.bookingService.getOrderById(orderId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.order.set(response);
        },
        error: () => {
          this.errorMessage.set('Unable to load order details from server. Please try again in a few moments.');
        },
        complete: () => {
          this.isLoading.set(false);
        }
      });
  }
}
