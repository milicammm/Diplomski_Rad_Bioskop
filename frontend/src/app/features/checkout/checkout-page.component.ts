import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../core/services/auth.service';
import { BookingService } from '../../core/services/booking.service';
import { ScreeningResponse, SeatResponse } from '../../core/models/booking.models';
import { evaluateDeal } from '../../core/services/deal-calculator';

@Component({
  selector: 'app-checkout-page',
  imports: [
    CommonModule,
    MatButtonModule,
    MatChipsModule,
    MatCardModule,
    MatDividerModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    RouterLink
  ],
  templateUrl: './checkout-page.component.html',
  styleUrls: ['./checkout-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly bookingService = inject(BookingService);
  private readonly authService = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly isLoading = signal(true);
  protected readonly isSubmitting = signal(false);
  protected readonly isRedirectingToPayment = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly screeningId = signal<number | null>(null);
  protected readonly requestedSeatIds = signal<number[]>([]);
  protected readonly screening = signal<ScreeningResponse | null>(null);
  protected readonly seats = signal<SeatResponse[]>([]);

  protected readonly selectedSeats = computed(() => {
    const requestedIds = this.requestedSeatIds();
    const availableSeats = this.seats();
    return availableSeats.filter((seat) => requestedIds.includes(seat.id));
  });

  protected readonly missingSeats = computed(() => {
    const selected = new Set(this.selectedSeats().map((seat) => seat.id));
    return this.requestedSeatIds().filter((seatId) => !selected.has(seatId));
  });

  protected readonly screeningPrice = computed(() => Number(this.screening()?.price ?? 0));

  protected readonly totalPrice = computed(() => this.screeningPrice() * this.selectedSeats().length);

  protected readonly activeDeal = computed(() => evaluateDeal(this.selectedSeats(), this.screening()));

  protected readonly discountedTotal = computed(() => {
    const deal = this.activeDeal();
    return deal ? this.totalPrice() * (100 - deal.percentage) / 100 : this.totalPrice();
  });

  ngOnInit() {
    this.route.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((params) => {
        this.errorMessage.set(null);

        const screeningId = Number(params.get('screeningId'));
        const seatIds = this.parseSeatIds(params.get('seats'));

        if (!Number.isFinite(screeningId) || screeningId <= 0 || seatIds.length === 0) {
          this.isLoading.set(false);
          this.errorMessage.set('Checkout data is invalid. Please select a new screening and seats.');
          return;
        }

        this.screeningId.set(screeningId);
        this.requestedSeatIds.set(seatIds);
        this.loadCheckoutData(screeningId);
      });
  }

  protected canConfirm() {
    return !!this.authService.currentUser()
      && !this.isSubmitting()
      && !!this.screening()
      && this.selectedSeats().length > 0
      && this.missingSeats().length === 0;
  }

  protected confirmOrder() {
    if (!this.canConfirm()) {
      return;
    }

    const user = this.authService.currentUser();
    const screeningId = this.screeningId();
    if (!user || !screeningId) {
      return;
    }

    if (this.isRedirectingToPayment()) {
      return;
    }

    this.isSubmitting.set(true);
    this.isRedirectingToPayment.set(false);

    this.bookingService.createOrder({
      userId: user.id,
      orderItems: this.selectedSeats().map((seat) => ({
        screeningId,
        seatId: seat.id,
        quantity: 1
      }))
    })
      .pipe(
        switchMap((order) => this.bookingService.createCheckoutSession(order.id)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (checkout) => {
          if (!checkout.url) {
            this.snackBar.open('Stripe session created but redirect URL is missing.', 'Close', { duration: 3500 });
            this.isSubmitting.set(false);
            this.isRedirectingToPayment.set(false);
            return;
          }

          this.isRedirectingToPayment.set(true);
          window.location.href = checkout.url;
        },
        error: (error) => {
          const rawMessage: string = error.error?.message ?? '';
          let errorMessage = 'Payment initialization failed. Please check your data and try again.';

          if (error.status === 409) {
            errorMessage = 'One or more selected seats are no longer available. Please select different seats.';
          } else if (error.status === 400 && rawMessage.includes('Insufficient')) {
            errorMessage = 'Not enough seats available for this screening.';
          } else if (error.status === 400 && rawMessage.includes('already')) {
            errorMessage = 'Some seats are already reserved. Please select different ones.';
          } else if (rawMessage) {
            errorMessage = rawMessage;
          }

          this.snackBar.open(errorMessage, 'Close', { duration: 4000 });
          this.isSubmitting.set(false);
          this.isRedirectingToPayment.set(false);
        }
      });
  }

  protected seatLabel(seat: SeatResponse) {
    return `${seat.rowNumber}${seat.seatNumber}`;
  }

  protected screeningDateTime() {
    const current = this.screening();
    if (!current) {
      return '';
    }

    const date = new Date(`${current.screeningDate}T${current.screeningTime}`);
    if (Number.isNaN(date.getTime())) {
      return `${current.screeningDate} ${current.screeningTime}`;
    }

    return new Intl.DateTimeFormat('en-GB', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }

  private loadCheckoutData(screeningId: number) {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.screening.set(null);
    this.seats.set([]);

    forkJoin({
      screening: this.bookingService.getScreeningById(screeningId),
      seats: this.bookingService.getAvailableSeatsForScreening(screeningId)
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ screening, seats }) => {
          this.screening.set(screening);
          this.seats.set(seats);

          if (this.selectedSeats().length === 0) {
            this.errorMessage.set('Selected seats are no longer available. Please select new seats.');
          }
        },
        error: (error) => {
          let errorMsg = 'Cannot load checkout data. Please try again.';

          if (error.error?.message) {
            errorMsg = error.error.message;
          } else if (error.status === 404) {
            errorMsg = 'Screening or seats not found. Please go back and select a new screening.';
          } else if (error.status === 409) {
            errorMsg = 'The seats you selected are no longer available. Please select different seats.';
          }

          this.errorMessage.set(errorMsg);
        },
        complete: () => {
          this.isLoading.set(false);
        }
      });
  }

  private parseSeatIds(raw: string | null) {
    if (!raw) {
      return [];
    }

    const parsed = raw
      .split(',')
      .map((part) => Number(part.trim()))
      .filter((value) => Number.isFinite(value) && value > 0);

    return Array.from(new Set(parsed));
  }
}
