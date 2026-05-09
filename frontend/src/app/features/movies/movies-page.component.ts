import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MoviesService } from '../../core/services/movies.service';
import { MovieResponse } from '../../core/models/movie.models';
import { BookingService } from '../../core/services/booking.service';
import { AuthService } from '../../core/services/auth.service';
import { OrderResponse } from '../../core/models/booking.models';

@Component({
  selector: 'app-movies-page',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
    RouterLink
  ],
  templateUrl: './movies-page.component.html',
  styleUrls: ['./movies-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MoviesPageComponent implements OnInit {
  private readonly moviesService = inject(MoviesService);
  private readonly bookingService = inject(BookingService);
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly loading = signal(true);
  protected readonly movies = signal<MovieResponse[]>([]);
  protected readonly orders = signal<OrderResponse[]>([]);
  protected readonly totalMovies = signal(0);
  protected readonly pageInfo = signal({ page: 0, size: 8, totalPages: 0 });

  protected readonly filtersForm = this.fb.nonNullable.group({
    keyword: [''],
    genre: [''],
    sortBy: ['createdAt'],
    direction: ['DESC']
  });

  protected readonly filteredMovies = signal<MovieResponse[]>([]);
  protected readonly pagedMovies = computed(() => {
    const items = this.filteredMovies();
    const info = this.pageInfo();
    const start = info.page * info.size;
    return items.slice(start, start + info.size);
  });
  protected readonly pageSummary = computed(() => {
    const total = this.filteredMovies().length;
    const info = this.pageInfo();
    if (!total) {
      return 'No movies found';
    }

    const start = info.page * info.size + 1;
    const end = Math.min(start + info.size - 1, total);
    return 'Showing ' + start + '-' + end + ' of ' + total;
  });

  protected readonly availableGenres = computed(() => {
    const genres = this.movies()
      .flatMap((movie) => this.normalizeTags(movie.genre))
      .filter(Boolean);

    return [...new Set(genres)].sort((left, right) => left.localeCompare(right));
  });

  protected readonly recommendedMovies = computed(() => {
    const profile = this.userPreferenceProfile();
    if (!profile) {
      return [];
    }

    return [...this.movies()]
      .filter((movie) => movie.active)
      .filter((movie) => !profile.watchedTitles.has(movie.title.toLowerCase()))
      .sort((left, right) => this.scoreMovie(right, profile) - this.scoreMovie(left, profile))
      .slice(0, 6);
  });

  protected readonly recommendationHint = computed(() => {
    const profile = this.userPreferenceProfile();
    if (!profile) {
      return '';
    }

    return 'Based on your booked genres and languages.';
  });

  private readonly userPreferenceProfile = computed(() => {
    const orders = this.orders().filter((order) => order.status === 'CONFIRMED' || order.status === 'PENDING');
    if (orders.length === 0) {
      return null;
    }

    const watchedTitles = new Set<string>();
    const genreCounts = new Map<string, number>();
    const languageCounts = new Map<string, number>();

    for (const order of orders) {
      for (const item of order.orderItems) {
        const title = (item.movieTitle || '').trim().toLowerCase();
        if (!title) {
          continue;
        }

        watchedTitles.add(title);
        const movie = this.movies().find((entry) => entry.title.trim().toLowerCase() === title);
        if (!movie) {
          continue;
        }

        for (const tag of this.normalizeTags(movie.genre)) {
          genreCounts.set(tag.toLowerCase(), (genreCounts.get(tag.toLowerCase()) || 0) + 1);
        }

        const language = (movie.language || '').trim().toLowerCase();
        if (language) {
          languageCounts.set(language, (languageCounts.get(language) || 0) + 1);
        }
      }
    }

    if (genreCounts.size === 0 && languageCounts.size === 0) {
      return null;
    }

    return { watchedTitles, genreCounts, languageCounts };
  });

  ngOnInit() {
    this.loadUserOrders();
    this.loadMovies();
  }

  protected refreshMovies() {
    this.movies.set([]);
    this.filteredMovies.set([]);
    this.loading.set(true);
    this.loadUserOrders();
    this.loadMovies();
  }

  protected applyFilters() {
    this.rebuildMoviesList(0, this.pageInfo().size);
  }

  protected handlePage(event: PageEvent) {
    this.rebuildMoviesList(event.pageIndex, event.pageSize);
  }

  protected clearFilters() {
    this.filtersForm.patchValue({
      keyword: '',
      genre: '',
      sortBy: 'createdAt',
      direction: 'DESC'
    });

    this.rebuildMoviesList(0, this.pageInfo().size);
  }

  protected ratingFor(movie: MovieResponse) {
    const rating = 4.4 + ((movie.id % 5) * 0.1);
    return rating.toFixed(1);
  }

  protected movieBadge(movie: MovieResponse) {
    return movie.active ? 'Now showing' : 'Coming soon';
  }

  protected posterUrl(movie: MovieResponse) {
    return movie.posterUrl || 'https://images.unsplash.com/photo-1489599513070-4b6f8f3f0f1e?auto=format&fit=crop&w=900&q=80';
  }

  protected genreTags(movie: MovieResponse) {
    return this.normalizeTags(movie.genre).slice(0, 3);
  }

  protected fillSuggestion(movie: MovieResponse) {
    this.filtersForm.patchValue({
      keyword: movie.title,
      genre: '',
      sortBy: 'createdAt',
      direction: 'DESC'
    });
    this.rebuildMoviesList(0, this.pageInfo().size);
  }

  private loadMovies() {
    this.loading.set(true);

    this.moviesService.getMovies({
      page: 0,
      size: 1000,
      sortBy: 'createdAt',
      direction: 'DESC'
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.movies.set(response.content);
          this.totalMovies.set(response.content.length);
          this.rebuildMoviesList(0, this.pageInfo().size);
        },
        error: () => {
          this.movies.set([]);
          this.filteredMovies.set([]);
          this.totalMovies.set(0);
          this.pageInfo.set({ page: 0, size: this.pageInfo().size, totalPages: 0 });
        },
        complete: () => {
          this.loading.set(false);
        }
      });
  }

  private rebuildMoviesList(page = this.pageInfo().page, size = this.pageInfo().size) {
    const raw = this.filtersForm.getRawValue();
    const keyword = raw.keyword.trim().toLowerCase();
    const genre = raw.genre.trim().toLowerCase();
    const sortBy = raw.sortBy as 'createdAt' | 'releaseDate' | 'title' | 'duration';
    const direction = raw.direction as 'ASC' | 'DESC';

    const filtered = this.movies()
      .filter((movie) => this.matchesKeyword(movie, keyword))
      .filter((movie) => !genre || this.normalizeTags(movie.genre).some((tag) => tag.toLowerCase() === genre))
      .sort((left, right) => this.compareMovies(left, right, sortBy, direction));

    this.filteredMovies.set(filtered);

    const safeSize = Math.max(1, size);
    const totalPages = filtered.length === 0 ? 0 : Math.ceil(filtered.length / safeSize);
    const safePage = totalPages === 0 ? 0 : Math.min(Math.max(page, 0), totalPages - 1);
    this.pageInfo.set({ page: safePage, size: safeSize, totalPages });
  }

  private matchesKeyword(movie: MovieResponse, keyword: string) {
    if (!keyword) {
      return true;
    }

    const haystack = [movie.title, movie.genre, movie.director, movie.language, movie.description, movie.cast]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();

    return haystack.includes(keyword);
  }

  private compareMovies(
    left: MovieResponse,
    right: MovieResponse,
    sortBy: 'createdAt' | 'releaseDate' | 'title' | 'duration',
    direction: 'ASC' | 'DESC'
  ) {
    const multiplier = direction === 'ASC' ? 1 : -1;

    let comparison = 0;
    switch (sortBy) {
      case 'releaseDate':
        comparison = new Date(left.releaseDate).getTime() - new Date(right.releaseDate).getTime();
        break;
      case 'title':
        comparison = left.title.localeCompare(right.title);
        break;
      case 'duration':
        comparison = left.duration - right.duration;
        break;
      case 'createdAt':
      default:
        comparison = new Date(left.createdAt || 0).getTime() - new Date(right.createdAt || 0).getTime();
        break;
    }

    return comparison * multiplier;
  }

  private normalizeTags(value: string) {
    return value
      .split(/[\/,&]/)
      .map((tag) => tag.trim())
      .filter(Boolean);
  }

  private scoreMovie(
    movie: MovieResponse,
    profile: {
      watchedTitles: Set<string>;
      genreCounts: Map<string, number>;
      languageCounts: Map<string, number>;
    }
  ) {
    let score = 0;

    for (const genre of this.normalizeTags(movie.genre)) {
      score += (profile.genreCounts.get(genre.toLowerCase()) || 0) * 10;
    }

    const language = (movie.language || '').trim().toLowerCase();
    if (language) {
      score += (profile.languageCounts.get(language) || 0) * 6;
    }

    score += Math.min(movie.soldTickets || 0, 500) * 0.01;

    if (movie.releaseDate) {
      const releaseTimestamp = new Date(movie.releaseDate).getTime();
      if (Number.isFinite(releaseTimestamp)) {
        const ageInDays = Math.max(0, (Date.now() - releaseTimestamp) / (1000 * 60 * 60 * 24));
        score += Math.max(0, 50 - ageInDays * 0.03);
      }
    }

    return score;
  }

  private loadUserOrders() {
    const user = this.authService.currentUser();
    if (!user) {
      this.orders.set([]);
      return;
    }

    this.bookingService.getOrdersByUserId(user.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (orders) => this.orders.set(orders),
        error: () => this.orders.set([])
      });
  }
}
