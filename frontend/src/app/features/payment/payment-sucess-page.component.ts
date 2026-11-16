import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription, interval } from 'rxjs';
import { BookingService } from '../../core/services/booking.service';
import { OrderResponse, OrderStatus } from '../../core/models/booking.models';

const TERMINAL_ORDER_STATUSES: OrderStatus[] = ['CONFIRMED', 'CANCELLED', 'EXPIRED'];
const POLL_INTERVAL_MS = 4000;
const POLL_TIMEOUT_MS = 120000;

@Component({
  selector: 'app-payment-success-page',
  imports: [
    CommonModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule],
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
  protected readonly isPolling = signal(false);
  protected readonly pollingTimedOut = signal(false);

  private pollingSubscription?: Subscription;

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
    this.pollingTimedOut.set(false);
    this.stopPolling();

    this.bookingService.getOrderById(orderId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.order.set(response);

          if (!this.isTerminalStatus(response.status)) {
            this.startPolling(orderId);
          }
        },
        error: (error) => {
          let errorMsg = 'Unable to load order details from server. Please try again in a few moments.';

          if (error.error?.message) {
            errorMsg = error.error.message;
          } else if (error.status === 404) {
            errorMsg = 'Order not found. Please contact support if you believe this is an error.';
          } else if (error.status === 401) {
            errorMsg = 'Your session has expired. Please log in again.';
          } else if (error.status === 0) {
            errorMsg = 'Network error. Please check your connection.';
          }

          this.errorMessage.set(errorMsg);
        },
        complete: () => {
          this.isLoading.set(false);
        }
      });
  }

  private startPolling(orderId: number) {
    this.isPolling.set(true);
    const deadline = Date.now() + POLL_TIMEOUT_MS;

    this.pollingSubscription = interval(POLL_INTERVAL_MS)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        if (Date.now() >= deadline) {
          this.pollingTimedOut.set(true);
          this.stopPolling();
          return;
        }

        this.bookingService.getOrderById(orderId).subscribe((response) => {
          this.order.set(response);

          if (this.isTerminalStatus(response.status)) {
            this.stopPolling();
          }
        });
      });
  }

  private stopPolling() {
    this.pollingSubscription?.unsubscribe();
    this.pollingSubscription = undefined;
    this.isPolling.set(false);
  }

  private isTerminalStatus(status: OrderStatus): boolean {
    return TERMINAL_ORDER_STATUSES.includes(status);
  }
}
