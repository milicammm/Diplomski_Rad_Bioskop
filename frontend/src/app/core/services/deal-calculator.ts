import { ScreeningResponse, SeatResponse } from '../models/booking.models';

export interface DealResult {
  percentage: number;
  label: string;
}

const GROUP_BOOKING_MIN_SEATS = 4;
const GROUP_BOOKING_PERCENTAGE = 20;
const VIP_COMBO_PERCENTAGE = 15;
const WEEKEND_SPECIAL_PERCENTAGE = 10;
const WEEKEND_EVENING_START_HOUR = 18;

/**
 * Mirrors the backend's DealCalculator (src/main/java/com/cineticket/service/impl/DealCalculator.java).
 * This is a client-side preview only — the authoritative discount is always computed
 * server-side when the order is created.
 */
export function evaluateDeal(seats: SeatResponse[], screening: ScreeningResponse | null): DealResult | null {
  if (!seats.length) {
    return null;
  }

  const groupBookingEligible = seats.length >= GROUP_BOOKING_MIN_SEATS;
  const vipComboEligible = seats.some((seat) => seat.type === 'VIP');
  const weekendSpecialEligible = !!screening && isWeekendEvening(screening);

  if (groupBookingEligible) {
    return { percentage: GROUP_BOOKING_PERCENTAGE, label: 'Group Booking discount' };
  }
  if (vipComboEligible) {
    return { percentage: VIP_COMBO_PERCENTAGE, label: 'Movie Night Combo discount' };
  }
  if (weekendSpecialEligible) {
    return { percentage: WEEKEND_SPECIAL_PERCENTAGE, label: 'Weekend Special discount' };
  }
  return null;
}

function isWeekendEvening(screening: ScreeningResponse): boolean {
  const date = new Date(`${screening.screeningDate}T00:00:00`);
  const dayOfWeek = date.getDay(); // 0=Sunday, 5=Friday, 6=Saturday
  const isWeekendDay = dayOfWeek === 5 || dayOfWeek === 6;

  const hour = Number(screening.screeningTime.split(':')[0]);
  const isEvening = hour >= WEEKEND_EVENING_START_HOUR;

  return isWeekendDay && isEvening;
}
