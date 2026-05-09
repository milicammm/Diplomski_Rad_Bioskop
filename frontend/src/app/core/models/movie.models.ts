export interface MovieResponse {
  id: number;
  title: string;
  description?: string;
  genre: string;
  duration: number;
  posterUrl?: string;
  trailerUrl?: string;
  releaseDate: string;
  director?: string;
  cast?: string;
  language?: string;
  active: boolean;
  soldTickets?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  numberOfElements: number;
  first: boolean;
  last: boolean;
}
