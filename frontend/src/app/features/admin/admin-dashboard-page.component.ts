import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { catchError, forkJoin, finalize, of } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BookingService } from '../../core/services/booking.service';
import { MoviesService } from '../../core/services/movies.service';
import { AdminHallRequest, AdminMovieRequest, AdminScreeningRequest, AdminSeatRequest } from '../../core/models/admin.models';
import { MovieResponse, PageResponse } from '../../core/models/movie.models';
import { AdminPaymentTransactionResponse, HallResponse, OrderResponse, ScreeningResponse, SeatResponse } from '../../core/models/booking.models';
import { RegisterRequest, UserResponse } from '../../core/models/auth.models';
import { buildAdminDashboardAnalytics } from './admin-dashboard.analytics';

@Component({
  selector: 'app-admin-dashboard-page',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSnackBarModule
  ],
  templateUrl: './admin-dashboard-page.component.html',
  styleUrls: ['./admin-dashboard-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminDashboardPageComponent implements OnInit {
  private readonly bookingService = inject(BookingService);
  private readonly moviesService = inject(MoviesService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly loadingData = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly movieSaving = signal(false);
  protected readonly hallSaving = signal(false);
  protected readonly seatSaving = signal(false);
  protected readonly screeningSaving = signal(false);
  protected readonly userSaving = signal(false);
  protected readonly paymentStatusUpdating = signal<number | null>(null);
  protected readonly paymentDeleting = signal<number | null>(null);

  protected readonly movies = signal<MovieResponse[]>([]);
  protected readonly screenings = signal<ScreeningResponse[]>([]);
  protected readonly orders = signal<OrderResponse[]>([]);
  protected readonly paymentTransactions = signal<AdminPaymentTransactionResponse[]>([]);
  protected readonly users = signal<UserResponse[]>([]);
  protected readonly halls = signal<HallResponse[]>([]);
  protected readonly seats = signal<SeatResponse[]>([]);
  protected readonly seatHallFilter = signal<number | null>(null);
  protected readonly editingMovieId = signal<number | null>(null);
  protected readonly editingHallId = signal<number | null>(null);
  protected readonly editingScreeningId = signal<number | null>(null);
  protected readonly editingUserId = signal<number | null>(null);

  protected readonly activeScreenings = computed(() => this.screenings().filter((screening) => screening.active).length);
  protected readonly pendingOrders = computed(() => this.orders().filter((order) => order.status === 'PENDING').length);
  protected readonly successfulPaymentsCount = computed(() => this.paymentTransactions().filter((payment) => payment.paymentStatus === 'SUCCEEDED').length);
  protected readonly totalHallCapacity = computed(() => this.halls().reduce((sum, hall) => sum + (hall.capacity || 0), 0));
  protected readonly latestOrders = computed(() => [...this.orders()].sort((a, b) => new Date(b.orderDate).getTime() - new Date(a.orderDate).getTime()).slice(0, 10));
  protected readonly analytics = computed(() => buildAdminDashboardAnalytics({
    orders: this.orders(),
    screenings: this.screenings(),
    movies: this.movies()
  }));
  protected readonly filteredSeats = computed(() => {
    const filter = this.seatHallFilter();
    if (!filter) {
      return this.seats();
    }

    return this.seats().filter((seat) => seat.hallId === filter);
  });

  protected readonly movieForm = this.fb.nonNullable.group({
    title: ['', [Validators.required]],
    description: [''],
    genre: ['', [Validators.required]],
    duration: [120, [Validators.required, Validators.min(1)]],
    posterUrl: [''],
    trailerUrl: [''],
    releaseDate: ['', [Validators.required]],
    director: [''],
    cast: [''],
    language: [''],
    active: [true]
  });

  protected readonly hallForm = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    capacity: [100, [Validators.required, Validators.min(1)]],
    description: ['']
  });

  protected readonly seatForm = this.fb.nonNullable.group({
    hallId: [0, [Validators.required, Validators.min(1)]],
    rowNumber: ['', [Validators.required]],
    seatNumber: [1, [Validators.required, Validators.min(1)]],
    type: ['REGULAR' as 'REGULAR' | 'VIP']
  });

  protected readonly screeningForm = this.fb.nonNullable.group({
    movieId: [0, [Validators.required, Validators.min(1)]],
    hallId: [0, [Validators.required, Validators.min(1)]],
    screeningDate: ['', [Validators.required]],
    screeningTime: ['', [Validators.required]],
    price: [500, [Validators.required, Validators.min(0)]],
    active: [true]
  });

  protected readonly userForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    role: ['CUSTOMER' as 'ADMIN' | 'CUSTOMER']
  });

  ngOnInit() {
    this.loadDashboard();
  }

  protected loadDashboard() {
    this.loadingData.set(true);
    this.errorMessage.set(null);
    const failedSections: string[] = [];
    let authErrorDetected = false;

    forkJoin({
      moviesPage: this.moviesService.getMovies({ page: 0, size: 200, sortBy: 'createdAt', direction: 'DESC' }).pipe(
        catchError(() => {
          failedSections.push('movies');
          return of(this.emptyMoviePage());
        })
      ),
      screenings: this.bookingService.getAllScreenings().pipe(catchError(() => {
        failedSections.push('screenings');
        return of([] as ScreeningResponse[]);
      })),
      orders: this.bookingService.getAllOrders().pipe(catchError((error) => {
        if (error instanceof HttpErrorResponse && (error.status === 401 || error.status === 403)) {
          authErrorDetected = true;
        }
        failedSections.push('orders');
        return of([] as OrderResponse[]);
      })),
      users: this.bookingService.getAllUsers().pipe(catchError((error) => {
        if (error instanceof HttpErrorResponse && (error.status === 401 || error.status === 403)) {
          authErrorDetected = true;
        }
        failedSections.push('users');
        return of([] as UserResponse[]);
      })),
      payments: this.bookingService.getPaymentTransactions(30).pipe(catchError((error) => {
        if (error instanceof HttpErrorResponse && (error.status === 401 || error.status === 403)) {
          authErrorDetected = true;
        }
        failedSections.push('payments');
        return of([] as AdminPaymentTransactionResponse[]);
      })),
      halls: this.bookingService.getAllHalls().pipe(catchError((error) => {
        if (error instanceof HttpErrorResponse && (error.status === 401 || error.status === 403)) {
          authErrorDetected = true;
        }
        failedSections.push('halls');
        return of([] as HallResponse[]);
      })),
      seats: this.bookingService.getAllSeats().pipe(catchError((error) => {
        if (error instanceof HttpErrorResponse && (error.status === 401 || error.status === 403)) {
          authErrorDetected = true;
        }
        failedSections.push('seats');
        return of([] as SeatResponse[]);
      }))
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ moviesPage, screenings, orders, users, payments, halls, seats }) => {
          this.movies.set(moviesPage.content);
            this.screenings.set(screenings);
            this.orders.set(orders);
            this.paymentTransactions.set(payments);
            this.users.set(users);
            this.halls.set(halls);
            this.seats.set(seats);

          if (failedSections.length > 0) {
            if (authErrorDetected) {
              this.errorMessage.set(`Some data failed to load (${failedSections.join(', ')}). Admin session is no longer valid, please log out and log back in.`);
            } else {
              this.errorMessage.set(`Some data failed to load (${failedSections.join(', ')}). Check if the backend is running.`);
            }
          }

          if (!this.seatHallFilter() && halls.length > 0) {
            this.seatHallFilter.set(halls[0].id);
          }

          this.syncEditSelectionAfterLoad();
        },
        error: () => {
          this.errorMessage.set('Cannot load admin data. Check backend status and permissions.');
        },
        complete: () => {
          this.loadingData.set(false);
        }
      });
  }

  protected editMovie(movie: MovieResponse) {
    this.editingMovieId.set(movie.id);
    this.movieForm.patchValue({
      title: movie.title,
      description: movie.description || '',
      genre: movie.genre,
      duration: movie.duration,
      posterUrl: movie.posterUrl || '',
      trailerUrl: movie.trailerUrl || '',
      releaseDate: movie.releaseDate,
      director: movie.director || '',
      cast: movie.cast || '',
      language: movie.language || '',
      active: movie.active
    });
  }

  protected resetMovieForm() {
    this.editingMovieId.set(null);
    this.movieForm.reset({
      title: '',
      description: '',
      genre: '',
      duration: 120,
      posterUrl: '',
      trailerUrl: '',
      releaseDate: '',
      director: '',
      cast: '',
      language: '',
      active: true
    });
  }

  protected submitMovie() {
    if (this.movieForm.invalid || this.movieSaving()) {
      this.movieForm.markAllAsTouched();
      return;
    }

    const payload = this.movieForm.getRawValue() as AdminMovieRequest;
    this.movieSaving.set(true);

    const request = this.editingMovieId()
      ? this.moviesService.updateMovie(this.editingMovieId() as number, payload)
      : this.moviesService.createMovie(payload);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.snackBar.open(this.editingMovieId() ? 'Movie updated.' : 'Movie created.', 'OK', { duration: 2400 });
        this.resetMovieForm();
        this.loadDashboard();
      },
      error: (error) => {
        this.snackBar.open(this.resolveApiError(error, 'Unable to save movie.'), 'Close', { duration: 3500 });
      },
      complete: () => {
        this.movieSaving.set(false);
      }
    });
  }

  protected removeMovie(movieId: number) {
    this.moviesService.deleteMovie(movieId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadDashboard(),
        error: () => this.snackBar.open('Unable to delete movie.', 'Close', { duration: 3200 })
      });
  }

  protected editHall(hall: HallResponse) {
    this.editingHallId.set(hall.id);
    this.hallForm.patchValue({
      name: hall.name,
      capacity: hall.capacity,
      description: hall.description || ''
    });
  }

  protected resetHallForm() {
    this.editingHallId.set(null);
    this.hallForm.reset({
      name: '',
      capacity: 100,
      description: ''
    });
  }

  protected submitHall() {
    if (this.hallForm.invalid || this.hallSaving()) {
      this.hallForm.markAllAsTouched();
      return;
    }

    const raw = this.hallForm.getRawValue();
    const payload: AdminHallRequest = {
      name: raw.name.trim(),
      capacity: raw.capacity,
      description: raw.description.trim()
    };

    if (!payload.name) {
      this.hallForm.controls.name.setErrors({ required: true });
      this.hallForm.controls.name.markAsTouched();
      return;
    }

    this.hallSaving.set(true);

    const request = this.editingHallId()
      ? this.bookingService.updateHall(this.editingHallId() as number, payload)
      : this.bookingService.createHall(payload);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.snackBar.open(this.editingHallId() ? 'Hall updated.' : 'Hall created.', 'OK', { duration: 2400 });
        this.resetHallForm();
        this.loadDashboard();
      },
      error: (error) => this.snackBar.open(this.resolveApiError(error, 'Unable to save hall.'), 'Close', { duration: 4500 }),
      complete: () => {
        this.hallSaving.set(false);
      }
    });
  }

  protected removeHall(hallId: number) {
    this.bookingService.deleteHall(hallId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadDashboard(),
        error: () => this.snackBar.open('Unable to delete hall.', 'Close', { duration: 3200 })
      });
  }

  protected editScreening(screening: ScreeningResponse) {
    this.editingScreeningId.set(screening.id);
    this.screeningForm.patchValue({
      movieId: screening.movieId,
      hallId: screening.hallId,
      screeningDate: screening.screeningDate,
      screeningTime: screening.screeningTime.slice(0, 5),
      price: Number(screening.price),
      active: screening.active
    });
  }

  protected resetScreeningForm() {
    this.editingScreeningId.set(null);
    this.screeningForm.reset({
      movieId: this.movies()[0]?.id || 0,
      hallId: this.halls()[0]?.id || 0,
      screeningDate: '',
      screeningTime: '',
      price: 500,
      active: true
    });
  }

  protected submitScreening() {
    if (this.halls().length === 0 || this.movies().length === 0) {
      this.snackBar.open('First add at least one movie and one theater.', 'Close', { duration: 3200 });
      return;
    }

    if (this.screeningForm.invalid || this.screeningSaving()) {
      this.screeningForm.markAllAsTouched();
      return;
    }

    const payload = this.screeningForm.getRawValue() as AdminScreeningRequest;
    this.screeningSaving.set(true);

    const request = this.editingScreeningId()
      ? this.bookingService.updateScreening(this.editingScreeningId() as number, payload)
      : this.bookingService.createScreening(payload);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.snackBar.open(this.editingScreeningId() ? 'Screening updated.' : 'Screening created.', 'OK', { duration: 2400 });
        this.resetScreeningForm();
        this.loadDashboard();
      },
      error: (error) => this.snackBar.open(this.resolveApiError(error, 'Unable to save screening.'), 'Close', { duration: 3500 }),
      complete: () => {
        this.screeningSaving.set(false);
      }
    });
  }

  protected removeScreening(screeningId: number) {
    this.bookingService.deleteScreening(screeningId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadDashboard(),
        error: () => this.snackBar.open('Unable to delete screening.', 'Close', { duration: 3200 })
      });
  }

  protected submitSeat() {
    if (this.halls().length === 0) {
      this.snackBar.open('First add a theater before adding seats.', 'Close', { duration: 3200 });
      return;
    }

    if (this.seatForm.invalid || this.seatSaving()) {
      this.seatForm.markAllAsTouched();
      return;
    }

    const payload = this.seatForm.getRawValue() as AdminSeatRequest;
    this.seatSaving.set(true);

    this.bookingService.createSeat(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.snackBar.open('Seat created.', 'OK', { duration: 2400 });
          this.resetSeatForm();
          this.loadDashboard();
        },
        error: (error) => this.snackBar.open(this.resolveApiError(error, 'Unable to save seat.'), 'Close', { duration: 3500 }),
        complete: () => {
          this.seatSaving.set(false);
        }
      });
  }

  protected resetSeatForm() {
    this.seatForm.reset({
      hallId: this.seatHallFilter() || this.halls()[0]?.id || 0,
      rowNumber: '',
      seatNumber: 1,
      type: 'REGULAR'
    });
  }

  protected removeSeat(seatId: number) {
    this.bookingService.deleteSeat(seatId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadDashboard(),
        error: () => this.snackBar.open('Unable to delete seat.', 'Close', { duration: 3200 })
      });
  }

  protected removeUser(userId: number) {
    this.bookingService.deleteUser(userId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadDashboard(),
        error: () => this.snackBar.open('Unable to delete user.', 'Close', { duration: 3200 })
      });
  }

  protected editUser(user: UserResponse) {
    this.editingUserId.set(user.id);
    this.userForm.patchValue({
      username: user.username,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      role: user.role as 'ADMIN' | 'CUSTOMER'
    });
    this.userForm.get('password')?.clearValidators();
    this.userForm.get('password')?.updateValueAndValidity();
  }

  protected resetUserForm() {
    this.editingUserId.set(null);
    this.userForm.reset({
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      role: 'CUSTOMER'
    });
    this.userForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
    this.userForm.get('password')?.updateValueAndValidity();
  }

  protected submitUser() {
    if (!this.userForm.valid) return;

    this.userSaving.set(true);
    const formValue = this.userForm.getRawValue();
    const payload: RegisterRequest = {
      username: formValue.username,
      email: formValue.email,
      password: formValue.password,
      firstName: formValue.firstName,
      lastName: formValue.lastName,
      phoneNumber: undefined
    };

    const request$ = this.editingUserId()
      ? this.bookingService.updateUser(this.editingUserId()!, payload)
      : this.bookingService.createUser(payload);

    request$
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.userSaving.set(false))
      )
      .subscribe({
        next: () => {
          this.snackBar.open(this.editingUserId() ? 'User updated.' : 'User created.', 'Close', { duration: 3200 });
          this.resetUserForm();
          this.loadDashboard();
        },
        error: (error) => {
          let message = 'Unable to save user.';
          
          if (error instanceof HttpErrorResponse && error.error?.message) {
            message = error.error.message;
          } else if (error instanceof HttpErrorResponse && error.status === 409) {
            message = error.error?.message?.includes('email')
              ? 'This email is already registered.'
              : 'This username is already taken.';
          }
          
          this.snackBar.open(message, 'Close', { duration: 4200 });
        }
      });
  }

  protected updatePaymentStatus(paymentId: number, newStatus: string) {
    this.paymentStatusUpdating.set(paymentId);
    this.bookingService.updatePayment(paymentId, newStatus)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.snackBar.open(`Payment status updated to ${newStatus}`, 'Close', { duration: 3200 });
          this.loadDashboard();
        },
        error: () => this.snackBar.open('Unable to update payment status.', 'Close', { duration: 3200 }),
        complete: () => this.paymentStatusUpdating.set(null)
      });
  }

  protected deletePaymentRecord(paymentId: number) {
    if (!confirm(`Delete payment record #${paymentId}? This only removes the local record, it does not refund the customer.`)) {
      return;
    }

    this.paymentDeleting.set(paymentId);

    this.bookingService.deletePayment(paymentId)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.paymentDeleting.set(null))
      )
      .subscribe({
        next: () => {
          this.snackBar.open(`Payment record #${paymentId} deleted.`, 'Close', { duration: 3200 });
          this.loadDashboard();
        },
        error: () => this.snackBar.open('Unable to delete payment record.', 'Close', { duration: 3200 })
      });
  }

  protected setOrderStatus(orderId: number, status: 'CONFIRMED' | 'CANCELLED') {
    this.bookingService.updateOrderStatus(orderId, status)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updatedOrder) => {
          this.orders.update((orders) => orders.map((order) => (order.id === updatedOrder.id ? updatedOrder : order)));
        },
        error: () => {
          this.errorMessage.set('Unable to update order status. Check backend availability.');
        }
      });
  }

  protected formatOrderDate(dateIso: string) {
    const date = new Date(dateIso);
    if (Number.isNaN(date.getTime())) {
      return dateIso;
    }

    return new Intl.DateTimeFormat('sr-RS', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }

  private syncEditSelectionAfterLoad() {
    if (!this.editingMovieId() && this.movies().length > 0) {
      this.movieForm.patchValue({ active: true });
    }

    if (!this.screeningForm.value.movieId && this.movies().length > 0) {
      this.screeningForm.patchValue({ movieId: this.movies()[0].id });
    }

    if (!this.screeningForm.value.hallId && this.halls().length > 0) {
      this.screeningForm.patchValue({ hallId: this.halls()[0].id });
    }

    if (!this.seatForm.value.hallId && this.halls().length > 0) {
      this.seatForm.patchValue({ hallId: this.halls()[0].id });
    }
  }

  private emptyMoviePage(): PageResponse<MovieResponse> {
    return {
      content: [],
      totalElements: 0,
      totalPages: 0,
      number: 0,
      size: 0,
      numberOfElements: 0,
      first: true,
      last: true
    };
  }

  private resolveApiError(error: unknown, fallback: string): string {
    if (!(error instanceof HttpErrorResponse)) {
      return fallback;
    }

    if (error.status === 0) {
      return 'Backend is not available. Check if the server is running.';
    }

    if (error.status === 401) {
      return 'Your session has expired. Please log in again.';
    }

    if (error.status === 403) {
      return 'You do not have admin privileges for this action. Please log in as an admin user.';
    }

    const body = error.error as { message?: string; errors?: string[] } | null;
    const validationMessage = body?.errors?.find((message) => !!message?.trim());
    if (validationMessage) {
      return validationMessage;
    }

    const message = body?.message?.trim();
    if (message) {
      const lower = message.toLowerCase();
      if (lower.includes('duplicate') || lower.includes('unique') || lower.includes('constraint')) {
        return 'An item with those details already exists. Please use different values.';
      }

      return message;
    }

    return fallback;
  }
}
