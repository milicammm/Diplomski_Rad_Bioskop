import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import {
  AdminPaymentTransactionResponse,
  CreateOrderRequest,
  HallResponse,
  OrderResponse,
  ScreeningResponse,
  SeatResponse,
  StripeCheckoutResponse,
  PaymentResponse
} from '../models/booking.models';
import { UserResponse, RegisterRequest } from '../models/auth.models';
import { AdminHallRequest, AdminScreeningRequest, AdminSeatRequest } from '../models/admin.models';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private readonly http = inject(HttpClient);

  getAllScreenings(): Observable<ScreeningResponse[]> {
    return this.http.get<ScreeningResponse[]>(`${API_BASE_URL}/api/screenings`);
  }

  createScreening(payload: AdminScreeningRequest): Observable<ScreeningResponse> {
    return this.http.post<ScreeningResponse>(`${API_BASE_URL}/api/screenings`, payload);
  }

  updateScreening(screeningId: number, payload: AdminScreeningRequest): Observable<ScreeningResponse> {
    return this.http.put<ScreeningResponse>(`${API_BASE_URL}/api/screenings/${screeningId}`, payload);
  }

  deleteScreening(screeningId: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/api/screenings/${screeningId}`);
  }

  getScreeningById(screeningId: number): Observable<ScreeningResponse> {
    return this.http.get<ScreeningResponse>(`${API_BASE_URL}/api/screenings/${screeningId}`);
  }

  getAvailableScreenings(movieId: number): Observable<ScreeningResponse[]> {
    return this.http.get<ScreeningResponse[]>(`${API_BASE_URL}/api/screenings/movie/${movieId}/available`);
  }

  getAvailableSeatsForScreening(screeningId: number): Observable<SeatResponse[]> {
    return this.http.get<SeatResponse[]>(`${API_BASE_URL}/api/seats/screening/${screeningId}/available`);
  }

  createOrder(payload: CreateOrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${API_BASE_URL}/api/orders`, payload);
  }

  getOrderById(orderId: number): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${API_BASE_URL}/api/orders/${orderId}`);
  }

  createCheckoutSession(orderId: number): Observable<StripeCheckoutResponse> {
    return this.http.post<StripeCheckoutResponse>(`${API_BASE_URL}/api/payments/checkout-session/${orderId}`, {});
  }

  getOrdersByUserId(userId: number): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${API_BASE_URL}/api/orders/user/${userId}`);
  }

  getAllOrders(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${API_BASE_URL}/api/orders`);
  }

  getPaymentTransactions(limit = 20): Observable<AdminPaymentTransactionResponse[]> {
    return this.http.get<AdminPaymentTransactionResponse[]>(`${API_BASE_URL}/api/admin/payments/transactions`, {
      params: { limit }
    });
  }

  updateOrderStatus(orderId: number, status: string): Observable<OrderResponse> {
    return this.http.put<OrderResponse>(`${API_BASE_URL}/api/orders/${orderId}/status`, null, {
      params: { status }
    });
  }

  getAllHalls(): Observable<HallResponse[]> {
    return this.http.get<HallResponse[]>(`${API_BASE_URL}/api/admin/halls`);
  }

  createHall(payload: AdminHallRequest): Observable<HallResponse> {
    return this.http.post<HallResponse>(`${API_BASE_URL}/api/admin/halls`, payload);
  }

  updateHall(hallId: number, payload: AdminHallRequest): Observable<HallResponse> {
    return this.http.put<HallResponse>(`${API_BASE_URL}/api/admin/halls/${hallId}`, payload);
  }

  deleteHall(hallId: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/api/admin/halls/${hallId}`);
  }

  getAllSeats(): Observable<SeatResponse[]> {
    return this.http.get<SeatResponse[]>(`${API_BASE_URL}/api/admin/seats`);
  }

  getSeatsByHallId(hallId: number): Observable<SeatResponse[]> {
    return this.http.get<SeatResponse[]>(`${API_BASE_URL}/api/admin/seats/hall/${hallId}`);
  }

  createSeat(payload: AdminSeatRequest): Observable<SeatResponse> {
    return this.http.post<SeatResponse>(`${API_BASE_URL}/api/admin/seats`, payload);
  }

  deleteSeat(seatId: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/api/admin/seats/${seatId}`);
  }

  // Admin users
  getAllUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${API_BASE_URL}/api/admin/users`);
  }

  createUser(payload: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${API_BASE_URL}/api/admin/users`, payload);
  }

  updateUser(userId: number, payload: RegisterRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${API_BASE_URL}/api/admin/users/${userId}`, payload);
  }

  deleteUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/api/admin/users/${userId}`);
  }

  // Admin payments 
  getAllPayments(): Observable<PaymentResponse[]> {
    return this.http.get<PaymentResponse[]>(`${API_BASE_URL}/api/admin/payments`);
  }

  getPaymentById(paymentId: number): Observable<PaymentResponse> {
    return this.http.get<PaymentResponse>(`${API_BASE_URL}/api/admin/payments/${paymentId}`);
  }

  createPaymentForOrder(orderId: number, paymentIntentId: string): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${API_BASE_URL}/api/admin/payments/create-for-order/${orderId}`, null, {
      params: { paymentIntentId }
    });
  }

  updatePayment(paymentId: number, status: string): Observable<PaymentResponse> {
    return this.http.put<PaymentResponse>(`${API_BASE_URL}/api/admin/payments/${paymentId}`, null, {
      params: { status }
    });
  }

  deletePayment(paymentId: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/api/admin/payments/${paymentId}`);
  }
}
