import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink } from '@angular/router';
import { MovieResponse } from '../../core/models/movie.models';
import { AuthService } from '../../core/services/auth.service';
import { MoviesService } from '../../core/services/movies.service';

@Component({
  selector: 'app-home-page',
  imports: [
    CommonModule,
    RouterLink,
    MatToolbarModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomePageComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly moviesService = inject(MoviesService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly isAuthenticated = this.authService.isAuthenticated;
  protected readonly isAdmin = this.authService.isAdmin;
  protected readonly currentUser = this.authService.currentUser;

  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly movies = signal<MovieResponse[]>([]);

  ngOnInit() {
    this.loadMovies();
  }

  protected loadMovies() {
    this.loading.set(true);
    this.error.set(null);

    this.moviesService
      .getMovies({
        page: 0,
        size: 8,
        sortBy: 'createdAt',
        direction: 'DESC'
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          const activeMovies = response.content.filter((movie) => movie.active);
          this.movies.set(activeMovies.length ? activeMovies : response.content);
          this.loading.set(false);
        },
        error: () => {
          this.movies.set([]);
          this.error.set('Movies are temporarily unavailable. Please refresh in a moment.');
          this.loading.set(false);
        }
      });
  }

  protected posterUrl(movie: MovieResponse): string {
  
    if (movie.posterUrl) {
      return movie.posterUrl;
    }
    return `https://images.unsplash.com/photo-1489599513070-4b6f8f3f0f1e?auto=format&fit=crop&w=500&q=80&txt=${encodeURIComponent(movie.title)}`;
  }

  protected ratingFor(movie: MovieResponse): string {
    const rating = 4.2 + ((movie.id % 7) * 0.1);
    return rating.toFixed(1);
  }

  protected shortDescription(movie: MovieResponse): string {
    const text = movie.description?.trim() || 'No description available yet.';
    return text.length > 150 ? `${text.slice(0, 150)}...` : text;
  }

  protected onPosterError(event: Event) {
    const image = event.target as HTMLImageElement | null;
    if (!image) {
      return;
    }

    console.warn(`Failed to load image from: ${image.src}`);

    image.src = 'https://images.unsplash.com/photo-1485846234645-a62644f84728?auto=format&fit=crop&w=500&q=80';
    
    image.dataset['isFallback'] = 'true';
  }

  protected trackMovie(_index: number, movie: MovieResponse) {
    return movie.id;
  }
}
