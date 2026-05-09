export interface AdminMovieRequest {
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
  active?: boolean;
}

export interface AdminHallRequest {
  name: string;
  capacity: number;
  description?: string;
}

export interface AdminSeatRequest {
  hallId: number;
  rowNumber: string;
  seatNumber: number;
  type: 'REGULAR' | 'VIP';
}

export interface AdminScreeningRequest {
  movieId: number;
  hallId: number;
  screeningDate: string;
  screeningTime: string;
  price: number;
  active?: boolean;
}
