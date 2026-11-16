import { OrderResponse } from '../../core/models/booking.models';
import { MovieResponse } from '../../core/models/movie.models';
import { ScreeningResponse } from '../../core/models/booking.models';

export interface AdminDashboardAnalytics {
  totalRevenue: number;
  ticketsSold: number;
  confirmedOrders: number;
  averageTicketPrice: number;
  occupancyRate: number;
  topMovieTitle: string;
  bestScreeningTitle: string;
}

export interface AdminDashboardAnalyticsInput {
  orders: OrderResponse[];
  screenings: ScreeningResponse[];
  movies: MovieResponse[];
}

export function buildAdminDashboardAnalytics(input: AdminDashboardAnalyticsInput): AdminDashboardAnalytics {
  const confirmedOrders = input.orders.filter((order) => order.status === 'CONFIRMED');
  const totalRevenue = confirmedOrders.reduce((sum, order) => sum + (order.totalPrice || 0), 0);
  const ticketsSold = confirmedOrders.reduce((sum, order) => sum + order.orderItems.reduce((itemSum, item) => itemSum + (item.quantity || 0), 0), 0);

  const totalSeats = input.screenings.reduce((sum, screening) => sum + (screening.totalSeats || 0), 0);
  const soldSeats = input.screenings.reduce((sum, screening) => sum + ((screening.totalSeats || 0) - (screening.availableSeats || 0)), 0);
  const occupancyRate = totalSeats > 0 ? Math.round((soldSeats / totalSeats) * 100) : 0;

  const movieSales = input.screenings.reduce<Map<string, number>>((acc, screening) => {
    const current = acc.get(screening.movieTitle) || 0;
    acc.set(screening.movieTitle, current + ((screening.totalSeats || 0) - (screening.availableSeats || 0)));
    return acc;
  }, new Map());

  const rankedMovie = [...movieSales.entries()].sort((a, b) => b[1] - a[1])[0];
  const topMovieTitle = rankedMovie?.[0] || input.movies[0]?.title || 'No data';

  const bestScreening = [...input.screenings].sort((a, b) => {
    const aSold = (a.totalSeats || 0) - (a.availableSeats || 0);
    const bSold = (b.totalSeats || 0) - (b.availableSeats || 0);
    return bSold - aSold;
  })[0];

  const bestScreeningTitle = bestScreening ? `${bestScreening.movieTitle} · ${bestScreening.hallName}` : 'No screenings';

  return {
    totalRevenue,
    ticketsSold,
    confirmedOrders: confirmedOrders.length,
    averageTicketPrice: ticketsSold > 0 ? Math.round(totalRevenue / ticketsSold) : 0,
    occupancyRate,
    topMovieTitle,
    bestScreeningTitle
  };
}
