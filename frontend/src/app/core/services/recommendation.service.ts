import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { MovieResponse } from '../models/movie.models';
import { SeatResponse } from '../models/booking.models';

@Injectable({ providedIn: 'root' })
export class RecommendationService {
  private readonly http = inject(HttpClient);

  getRecommendedMovies(userId: number, limit = 6): Observable<MovieResponse[]> {
    const params = new HttpParams().set('userId', userId).set('limit', limit);
    return this.http.get<MovieResponse[]>(`${API_BASE_URL}/api/recommendations/movies`, { params });
  }

  getRecommendedSeats(screeningId: number, userId: number | null, limit = 8): Observable<SeatResponse[]> {
    let params = new HttpParams().set('screeningId', screeningId).set('limit', limit);
    if (userId != null) {
      params = params.set('userId', userId);
    }

    return this.http.get<SeatResponse[]>(`${API_BASE_URL}/api/recommendations/seats`, { params });
  }
}
