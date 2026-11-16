import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MoviesService } from '../../core/services/movies.service';
import { MovieResponse } from '../../core/models/movie.models';
import { BookingService } from '../../core/services/booking.service';
import { RecommendationService } from '../../core/services/recommendation.service';
import { OrderResponse, ScreeningResponse, SeatResponse } from '../../core/models/booking.models';
import { AuthService } from '../../core/services/auth.service';
import { evaluateDeal } from '../../core/services/deal-calculator';

@Component({
  selector: 'app-tickets-page',
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './tickets-page.component.html',
  styleUrls: ['./tickets-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TicketsPageComponent implements OnInit {
  private readonly moviesService = inject(MoviesService);
  private readonly bookingService = inject(BookingService);
  private readonly recommendationService = inject(RecommendationService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly movies = signal<MovieResponse[]>([]);
  protected readonly screenings = signal<ScreeningResponse[]>([]);
  protected readonly seats = signal<SeatResponse[]>([]);
  protected readonly selectedMovieId = signal<number | null>(null);
  protected readonly selectedScreeningId = signal<number | null>(null);
  protected readonly selectedSeatIds = signal<number[]>([]);
  protected readonly selectedScreeningPrice = signal(0);
  protected readonly loadingMovies = signal(false);
  protected readonly loadingScreenings = signal(false);
  protected readonly loadingSeats = signal(false);
  protected readonly loadingOrders = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly orders = signal<OrderResponse[]>([]);
  protected readonly cancelingOrderId = signal<number | null>(null);
  protected readonly seatTypeFilter = signal<'ALL' | 'VIP' | 'REGULAR'>('ALL');
  protected readonly seatTypeFilters = [
    { value: 'ALL' as const, label: 'All seats' },
    { value: 'VIP' as const, label: 'VIP' },
    { value: 'REGULAR' as const, label: 'Regular' }
  ];
  protected readonly historyStatusFilter = signal<'ALL' | 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED'>('ALL');
  protected readonly historyFromDate = signal('');
  protected readonly historyStatuses = [
    { value: 'ALL' as const, label: 'All statuses' },
    { value: 'PENDING' as const, label: 'Pending' },
    { value: 'CONFIRMED' as const, label: 'Confirmed' },
    { value: 'CANCELLED' as const, label: 'Cancelled' },
    { value: 'EXPIRED' as const, label: 'Expired' }
  ];

  protected readonly filteredOrders = computed(() => {
    const statusFilter = this.historyStatusFilter();
    const fromDate = this.historyFromDate();

    return this.orders().filter((order) => {
      const matchesStatus = statusFilter === 'ALL' || order.status === statusFilter;
      const matchesDate = !fromDate || order.orderDate.slice(0, 10) >= fromDate;
      return matchesStatus && matchesDate;
    });
  });

  protected readonly visibleSeats = computed(() => {
    const filter = this.seatTypeFilter();
    if (filter === 'ALL') {
      return this.seats();
    }

    return this.seats().filter((seat) => seat.type === filter);
  });

  protected readonly groupedSeats = computed(() => {
    const sortedSeats = [...this.visibleSeats()].sort((left, right) => {
      const leftRow = this.rowLabelToRank(left.rowNumber);
      const rightRow = this.rowLabelToRank(right.rowNumber);

      if (leftRow === rightRow) {
        return left.seatNumber - right.seatNumber;
      }

      return leftRow - rightRow;
    });

    const groups = new Map<string, { rowKey: string; rowLabel: string; rowRank: number; seats: SeatResponse[]; seatGroups: SeatResponse[][] }>();

    for (const seat of sortedSeats) {
      const rowLabel = seat.rowNumber.trim().toUpperCase() || 'UNKNOWN';
      const rowKey = rowLabel;
      const rowRank = this.rowLabelToRank(rowLabel);
      const group = groups.get(rowKey) || { rowKey, rowLabel, rowRank, seats: [], seatGroups: [[], []] };
      group.seats.push(seat);
      groups.set(rowKey, group);
    }

    const rows = [...groups.values()].sort((left, right) => left.rowRank - right.rowRank);

    // For visual aisles, split seats per row into two groups (left/right) so template can render a gap.
    for (const row of rows) {
      const seatsCopy = [...row.seats];
      const mid = Math.ceil(seatsCopy.length / 2);
      row.seatGroups = [seatsCopy.slice(0, mid), seatsCopy.slice(mid)];
    }

    return rows;
  });

  protected readonly recommendedSeats = signal<SeatResponse[]>([]);

  protected readonly recommendedSeatIds = computed(() => new Set(this.recommendedSeats().map((seat) => seat.id)));

  protected readonly hasBookingHistory = computed(() =>
    this.orders().some((order) => order.status !== 'CANCELLED' && order.orderItems.length > 0)
  );

  protected readonly selectedScreening = computed(() => {
    const screeningId = this.selectedScreeningId();
    return this.screenings().find((screening) => screening.id === screeningId) ?? null;
  });

  protected readonly selectedSeats = computed(() => {
    const ids = new Set(this.selectedSeatIds());
    return this.seats().filter((seat) => ids.has(seat.id));
  });

  protected readonly selectedSeatLabels = computed(() =>
    [...this.selectedSeats()]
      .sort((a, b) => (a.rowNumber === b.rowNumber ? a.seatNumber - b.seatNumber : a.rowNumber.localeCompare(b.rowNumber)))
      .map((seat) => this.seatLabel(seat))
      .join(', ')
  );

  protected readonly activeDeal = computed(() => evaluateDeal(this.selectedSeats(), this.selectedScreening()));

  protected readonly recommendationTitle = computed(() => {
    return this.hasBookingHistory() ? 'Recommended for you' : 'Best seats in this hall';
  });

  protected readonly recommendationSubtitle = computed(() => {
    return this.hasBookingHistory()
      ? 'Personalized from your previous bookings and how other guests book this hall.'
      : 'No booking history yet, so this is based on the hall center and how other guests book it.';
  });

  ngOnInit() {
    this.route.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((params) => {
        const movieId = Number(params.get('movieId'));
        if (Number.isFinite(movieId) && movieId > 0) {
          this.selectedMovieId.set(movieId);
        }
      });

    this.loadMovies();
    this.loadUserOrders();
  }

  protected refresh() {
    this.errorMessage.set(null);
    this.loadMovies();
    this.loadUserOrders();
  }

  protected onHistoryFromDateChange(event: Event) {
    const target = event.target as HTMLInputElement | null;
    this.historyFromDate.set(target?.value || '');
  }

  protected clearHistoryFilters() {
    this.historyStatusFilter.set('ALL');
    this.historyFromDate.set('');
  }

  protected onMovieChange(event: MatSelectChange) {
    const movieId = Number(event.value);
    this.selectedMovieId.set(Number.isFinite(movieId) ? movieId : null);
    this.selectedScreeningId.set(null);
    this.selectedSeatIds.set([]);
    this.seats.set([]);
    this.recommendedSeats.set([]);
    this.selectedScreeningPrice.set(0);

    if (this.selectedMovieId()) {
      this.loadScreenings(this.selectedMovieId() as number);
    } else {
      this.screenings.set([]);
    }
  }

  protected selectScreening(screening: ScreeningResponse) {
    this.selectedScreeningId.set(screening.id);
    this.selectedScreeningPrice.set(Number(screening.price) || 0);
    this.selectedSeatIds.set([]);
    this.loadSeats(screening.id);
    this.loadSeatRecommendations(screening.id);
  }

  protected toggleSeat(seatId: number) {
    const seat = this.seats().find((item) => item.id === seatId);
    if (!seat || seat.reserved) {
      this.errorMessage.set('That seat is already reserved. Choose another one.');
      return;
    }

    this.errorMessage.set(null);

    const selected = this.selectedSeatIds();
    if (selected.includes(seatId)) {
      this.selectedSeatIds.set(selected.filter((id) => id !== seatId));
      return;
    }

    this.selectedSeatIds.set([...selected, seatId]);
  }

  protected isSeatSelected(seatId: number) {
    return this.selectedSeatIds().includes(seatId);
  }

  protected seatLabel(seat: SeatResponse) {
    return `${seat.rowNumber}${seat.seatNumber}`;
  }

  protected canContinueToCheckout() {
    return !!this.selectedScreeningId() && this.selectedSeatIds().length > 0;
  }

  protected estimatedTotal() {
    const subtotal = this.selectedScreeningPrice() * this.selectedSeatIds().length;
    const deal = this.activeDeal();
    return deal ? subtotal * (100 - deal.percentage) / 100 : subtotal;
  }

  protected estimatedSubtotal() {
    return this.selectedScreeningPrice() * this.selectedSeatIds().length;
  }

  protected goToCheckout() {
    const screeningId = this.selectedScreeningId();
    const seatIds = this.selectedSeatIds();

    if (!screeningId || seatIds.length === 0) {
      return;
    }

    this.router.navigate(['/checkout'], {
      queryParams: {
        screeningId,
        seats: seatIds.join(',')
      }
    });
  }

  protected formatScreeningDateTime(screening: ScreeningResponse) {
    const date = new Date(`${screening.screeningDate}T${screening.screeningTime}`);
    if (Number.isNaN(date.getTime())) {
      return `${screening.screeningDate} ${screening.screeningTime}`;
    }

    return new Intl.DateTimeFormat('en-GB', {
      weekday: 'short',
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }

  protected cancelOrder(order: OrderResponse) {
    if (order.status !== 'PENDING' || this.cancelingOrderId() !== null) {
      return;
    }

    if (!confirm(`Cancel order #${order.id}? This cannot be undone.`)) {
      return;
    }

    this.cancelingOrderId.set(order.id);
    this.errorMessage.set(null);

    this.bookingService.cancelOrder(order.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.loadUserOrders();
        },
        error: (error) => {
          this.errorMessage.set(error.error?.message || 'Unable to cancel order. Please try again.');
          this.cancelingOrderId.set(null);
        },
        complete: () => {
          this.cancelingOrderId.set(null);
        }
      });
  }

  protected formatOrderDate(dateIso: string) {
    const date = new Date(dateIso);
    if (Number.isNaN(date.getTime())) {
      return dateIso;
    }

    return new Intl.DateTimeFormat('en-GB', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }

  private loadMovies() {
    this.loadingMovies.set(true);
    this.errorMessage.set(null);

    this.moviesService.getMovies({ page: 0, size: 50, sortBy: 'title', direction: 'ASC' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.movies.set(response.content);

          if (!response.content.length) {
            this.screenings.set([]);
            return;
          }

          const currentSelection = this.selectedMovieId();
          const selectedMovie = response.content.find((movie) => movie.id === currentSelection) || response.content[0];
          this.selectedMovieId.set(selectedMovie.id);
          this.loadScreenings(selectedMovie.id);
        },
        error: () => {
          this.movies.set([]);
          this.screenings.set([]);
          this.seats.set([]);
          this.errorMessage.set('Cannot load movies at the moment.');
        },
        complete: () => {
          this.loadingMovies.set(false);
        }
      });
  }

  private loadScreenings(movieId: number) {
    this.loadingScreenings.set(true);
    this.errorMessage.set(null);
    this.screenings.set([]);
    this.selectedScreeningId.set(null);
    this.seats.set([]);
    this.selectedSeatIds.set([]);

    this.bookingService.getAvailableScreenings(movieId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (screenings) => {
          const sorted = [...screenings].sort((a, b) => {
            const aDate = new Date(`${a.screeningDate}T${a.screeningTime}`).getTime();
            const bDate = new Date(`${b.screeningDate}T${b.screeningTime}`).getTime();
            return aDate - bDate;
          });
          this.screenings.set(sorted);

          if (sorted.length > 0) {
            this.selectScreening(sorted[0]);
          }
        },
        error: () => {
          this.screenings.set([]);
          this.errorMessage.set('Cannot load screenings for selected movie.');
        },
        complete: () => {
          this.loadingScreenings.set(false);
        }
      });
  }

  private loadSeats(screeningId: number) {
    this.loadingSeats.set(true);
    this.errorMessage.set(null);

    this.bookingService.getAvailableSeatsForScreening(screeningId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (seats) => {
          const sorted = [...seats].sort((a, b) => {
            if (a.rowNumber === b.rowNumber) {
              return a.seatNumber - b.seatNumber;
            }

            return a.rowNumber.localeCompare(b.rowNumber);
          });
          this.seats.set(sorted);
        },
        error: () => {
          this.seats.set([]);
          this.errorMessage.set('Cannot load seats for selected screening.');
        },
        complete: () => {
          this.loadingSeats.set(false);
        }
      });
  }

  private loadUserOrders() {
    const user = this.authService.currentUser();
    if (!user) {
      this.orders.set([]);
      this.loadingOrders.set(false);
      return;
    }

    this.loadingOrders.set(true);

    this.bookingService.getOrdersByUserId(user.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (orders) => {
          const sorted = [...orders]
            .sort((a, b) => new Date(b.orderDate).getTime() - new Date(a.orderDate).getTime());
          this.orders.set(sorted);
        },
        error: () => {
          this.orders.set([]);
          this.loadingOrders.set(false);
        },
        complete: () => {
          this.loadingOrders.set(false);
        }
      });
  }

  private loadSeatRecommendations(screeningId: number) {
    const user = this.authService.currentUser();

    this.recommendationService.getRecommendedSeats(screeningId, user ? user.id : null)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (seats) => this.recommendedSeats.set(seats),
        error: () => this.recommendedSeats.set([])
      });
  }

  private rowLabelToRank(rowLabel: string) {
    const normalized = rowLabel.trim().toUpperCase();
    if (!normalized) {
      return Number.NaN;
    }

    return Array.from(normalized).reduce((rank, character) => {
      const charCode = character.charCodeAt(0);
      if (charCode < 65 || charCode > 90) {
        return rank;
      }

      return rank * 26 + (charCode - 64);
    }, 0);
  }
}
