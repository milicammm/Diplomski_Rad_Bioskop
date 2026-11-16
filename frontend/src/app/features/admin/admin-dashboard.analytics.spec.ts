import { describe, expect, it } from 'vitest';
import { buildAdminDashboardAnalytics } from './admin-dashboard.analytics';

describe('buildAdminDashboardAnalytics', () => {
  it('aggregates revenue, sales volume, and occupancy into a dashboard summary', () => {
    const analytics = buildAdminDashboardAnalytics({
      orders: [
        {
          id: 1,
          userId: 1,
          username: 'alice',
          totalPrice: 30,
          orderDate: '2026-07-20T20:00:00Z',
          status: 'CONFIRMED',
          orderItems: [
            { id: 1, screeningId: 10, movieTitle: 'Interstellar', hallName: 'Hall A', screeningDateTime: '2026-07-20T20:00:00Z', seatId: 1, seatInfo: 'A-1', quantity: 2, price: 15, subtotal: 30 }
          ],
          createdAt: '2026-07-20T20:00:00Z',
          updatedAt: '2026-07-20T20:00:00Z'
        },
        {
          id: 2,
          userId: 2,
          username: 'bob',
          totalPrice: 20,
          orderDate: '2026-07-21T20:00:00Z',
          status: 'PENDING',
          orderItems: [
            { id: 2, screeningId: 11, movieTitle: 'Dune', hallName: 'Hall B', screeningDateTime: '2026-07-21T20:00:00Z', seatId: 2, seatInfo: 'B-2', quantity: 2, price: 10, subtotal: 20 }
          ],
          createdAt: '2026-07-21T20:00:00Z',
          updatedAt: '2026-07-21T20:00:00Z'
        }
      ],
      screenings: [
        { id: 10, movieId: 1, movieTitle: 'Interstellar', hallId: 1, hallName: 'Hall A', screeningDate: '2026-07-20', screeningTime: '20:00', price: 15, availableSeats: 8, totalSeats: 10, active: true, soldTickets: 2 },
        { id: 11, movieId: 2, movieTitle: 'Dune', hallId: 2, hallName: 'Hall B', screeningDate: '2026-07-21', screeningTime: '20:00', price: 10, availableSeats: 4, totalSeats: 10, active: true, soldTickets: 6 }
      ],
      movies: [{ id: 1, title: 'Interstellar', description: '', genre: 'Sci-Fi', duration: 169, posterUrl: '', trailerUrl: '', releaseDate: '2014-11-07', director: 'Christopher Nolan', cast: '', language: 'English', active: true, createdAt: '2024-01-01' }]
    });

    expect(analytics.totalRevenue).toBe(30);
    expect(analytics.ticketsSold).toBe(2);
    expect(analytics.confirmedOrders).toBe(1);
    expect(analytics.averageTicketPrice).toBe(15);
    expect(analytics.occupancyRate).toBe(40);
    expect(analytics.topMovieTitle).toBe('Dune');
    expect(analytics.bestScreeningTitle).toBe('Dune · Hall B');
  });
});
