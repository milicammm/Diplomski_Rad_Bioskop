import { describe, expect, it } from 'vitest';
import { evaluateDeal } from './deal-calculator';
import { ScreeningResponse, SeatResponse } from '../models/booking.models';

function seat(type: SeatResponse['type'], id = 1): SeatResponse {
  return { id, hallId: 1, hallName: 'Hall A', rowNumber: 'A', seatNumber: id, type, reserved: false };
}

function screening(screeningDate: string, screeningTime: string): ScreeningResponse {
  return {
    id: 1,
    movieId: 1,
    movieTitle: 'Interstellar',
    hallId: 1,
    hallName: 'Hall A',
    screeningDate,
    screeningTime,
    price: 10,
    availableSeats: 10,
    totalSeats: 10,
    active: true
  };
}

describe('evaluateDeal', () => {
  it('returns null when there are no seats', () => {
    expect(evaluateDeal([], screening('2026-01-05', '19:00'))).toBeNull();
  });

  it('returns null for a regular weekday matinee with few seats', () => {
    const seats = [seat('REGULAR', 1), seat('REGULAR', 2)];
    // 2026-01-05 is a Monday
    expect(evaluateDeal(seats, screening('2026-01-05', '14:00'))).toBeNull();
  });

  it('applies the Group Booking discount for 4+ seats', () => {
    const seats = [seat('REGULAR', 1), seat('REGULAR', 2), seat('REGULAR', 3), seat('REGULAR', 4)];
    const result = evaluateDeal(seats, screening('2026-01-05', '14:00'));
    expect(result).toEqual({ percentage: 20, label: 'Group Booking discount' });
  });

  it('applies the Movie Night Combo discount for a VIP seat', () => {
    const seats = [seat('VIP', 1)];
    const result = evaluateDeal(seats, screening('2026-01-05', '14:00'));
    expect(result).toEqual({ percentage: 15, label: 'Movie Night Combo discount' });
  });

  it('applies the Weekend Special discount for a Friday evening screening', () => {
    const seats = [seat('REGULAR', 1)];
    // 2026-01-09 is a Friday
    const result = evaluateDeal(seats, screening('2026-01-09', '19:00'));
    expect(result).toEqual({ percentage: 10, label: 'Weekend Special discount' });
  });

  it('does not treat a Saturday afternoon as a weekend evening', () => {
    const seats = [seat('REGULAR', 1)];
    // 2026-01-10 is a Saturday
    expect(evaluateDeal(seats, screening('2026-01-10', '15:00'))).toBeNull();
  });

  it('does not stack discounts - group booking wins over VIP', () => {
    const seats = [seat('VIP', 1), seat('REGULAR', 2), seat('REGULAR', 3), seat('REGULAR', 4)];
    const result = evaluateDeal(seats, screening('2026-01-09', '19:00'));
    expect(result).toEqual({ percentage: 20, label: 'Group Booking discount' });
  });
});
