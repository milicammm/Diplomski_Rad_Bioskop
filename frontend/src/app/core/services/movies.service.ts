import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { MovieResponse, PageResponse } from '../models/movie.models';
import { AdminMovieRequest } from '../models/admin.models';

export interface MoviesQuery {
  page: number;
  size: number;
  sortBy: 'id' | 'title' | 'genre' | 'duration' | 'releaseDate' | 'director' | 'language' | 'active' | 'createdAt' | 'updatedAt';
  direction: 'ASC' | 'DESC';
  keyword?: string;
}

@Injectable({ providedIn: 'root' })
export class MoviesService {
  private readonly http = inject(HttpClient);

  getMovies(query: MoviesQuery): Observable<PageResponse<MovieResponse>> {
    let params = this.buildParams(query);
  
    params = params.set('_t', Date.now().toString());

    if (query.keyword?.trim()) {
      return this.http.get<PageResponse<MovieResponse>>(`${API_BASE_URL}/api/movies/search`, { params });
    }

    return this.http.get<PageResponse<MovieResponse>>(`${API_BASE_URL}/api/movies`, { params });
  }

  createMovie(payload: AdminMovieRequest): Observable<MovieResponse> {
    return this.http.post<MovieResponse>(`${API_BASE_URL}/api/movies`, payload);
  }

  updateMovie(movieId: number, payload: AdminMovieRequest): Observable<MovieResponse> {
    return this.http.put<MovieResponse>(`${API_BASE_URL}/api/movies/${movieId}`, payload);
  }

  deleteMovie(movieId: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/api/movies/${movieId}`);
  }

  private buildParams(query: MoviesQuery) {
    let params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size)
      .set('sortBy', query.sortBy)
      .set('direction', query.direction);

    if (query.keyword?.trim()) {
      params = params.set('keyword', query.keyword.trim());
    }

    return params;
  }
}
