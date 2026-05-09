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
import { OrderResponse, ScreeningResponse, SeatResponse } from '../../core/models/booking.models';
import { AuthService } from '../../core/services/auth.service';

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

    const groups = new Map<string, { rowKey: string; rowLabel: string; rowRank: number; seats: SeatResponse[] }>();

    for (const seat of sortedSeats) {
      const rowLabel = seat.rowNumber.trim().toUpperCase() || 'UNKNOWN';
      const rowKey = rowLabel;
      const rowRank = this.rowLabelToRank(rowLabel);
      const group = groups.get(rowKey) || { rowKey, rowLabel, rowRank, seats: [] };
      group.seats.push(seat);
      groups.set(rowKey, group);
    }

    return [...groups.values()].sort((left, right) => left.rowRank - right.rowRank);
  });

  protected readonly recommendedSeats = computed(() => {
    const seats = this.visibleSeats();
    if (seats.length === 0) {
      return [];
    }

    const preference = this.buildSeatPreferenceProfile();
    const rowRanks = this.uniqueRowRanks(seats);
    const centerRowRank = rowRanks[Math.floor(rowRanks.length / 2)] ?? null;
    const seatNumbers = [...new Set(seats.map((seat) => seat.seatNumber))].sort((a, b) => a - b);
    const centerSeatNumber = seatNumbers[Math.floor(seatNumbers.length / 2)] ?? null;

    return [...seats]
      .sort((left, right) => this.scoreSeat(right, preference, centerRowRank, centerSeatNumber) - this.scoreSeat(left, preference, centerRowRank, centerSeatNumber))
      .slice(0, 8);
  });

  protected readonly recommendedSeatIds = computed(() => new Set(this.recommendedSeats().map((seat) => seat.id)));

  protected readonly recommendationTitle = computed(() => {
    return this.buildSeatPreferenceProfile() ? 'Recommended for you' : 'Best seats in this hall';
  });

  protected readonly recommendationSubtitle = computed(() => {
    return this.buildSeatPreferenceProfile()
      ? 'Personalized from your previous bookings and the current hall layout.'
      : 'No booking history yet, so this is based on the center of the current hall.';
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

  private buildSeatPreferenceProfile() {
    const preferredOrders = this.orders().filter((order) => order.status !== 'CANCELLED');
    const seatObservations = preferredOrders.flatMap((order) =>
      order.orderItems
        .map((item) => this.parseSeatInfo(item.seatInfo))
        .filter((value): value is { rowRank: number; seatNumber: number } => value !== null)
    );

    if (seatObservations.length === 0) {
      return null;
    }

    return {
      preferredRowRank: this.mostCommonValue(seatObservations.map((entry) => entry.rowRank)),
      preferredSeatNumber: this.mostCommonValue(seatObservations.map((entry) => entry.seatNumber))
    };
  }

  private parseSeatInfo(seatInfo: string | null | undefined) {
    if (!seatInfo) {
      return null;
    }

    const [rowLabel, seatNumberRaw] = seatInfo.split('-');
    const rowRank = this.rowLabelToRank(rowLabel);
    const seatNumber = Number(seatNumberRaw);

    if (!Number.isFinite(rowRank) || !Number.isFinite(seatNumber) || seatNumber <= 0) {
      return null;
    }

    return { rowRank, seatNumber };
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

  private uniqueRowRanks(seats: SeatResponse[]) {
    return [...new Set(seats.map((seat) => this.rowLabelToRank(seat.rowNumber)).filter((rank) => Number.isFinite(rank)))].sort((a, b) => a - b);
  }

  private scoreSeat(
    seat: SeatResponse,
    preference: { preferredRowRank: number | null; preferredSeatNumber: number | null } | null,
    centerRowRank: number | null,
    centerSeatNumber: number | null
  ) {
    const rowRank = this.rowLabelToRank(seat.rowNumber);
    let score = 0;

    if (preference?.preferredRowRank) {
      score -= Math.abs(rowRank - preference.preferredRowRank) * 12;
    } else if (centerRowRank) {
      score -= Math.abs(rowRank - centerRowRank) * 8;
    }

    if (preference?.preferredSeatNumber) {
      score -= Math.abs(seat.seatNumber - preference.preferredSeatNumber) * 4;
    } else if (centerSeatNumber) {
      score -= Math.abs(seat.seatNumber - centerSeatNumber) * 2;
    }

    if (seat.type === this.seatTypeFilter()) {
      score += 1;
    }

    return score;
  }

  private mostCommonValue(values: number[]) {
    if (values.length === 0) {
      return null;
    }

    const counts = new Map<number, number>();
    for (const value of values) {
      counts.set(value, (counts.get(value) || 0) + 1);
    }

    let bestValue: number | null = null;
    let bestCount = 0;
    counts.forEach((count, value) => {
      if (count > bestCount) {
        bestValue = value;
        bestCount = count;
      }
    });

    return bestValue;
  }
}
