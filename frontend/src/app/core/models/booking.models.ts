export interface ScreeningResponse {
  id: number;
  movieId: number;
  movieTitle: string;
  posterUrl?: string;
  hallId: number;
  hallName: string;
  screeningDate: string;
  screeningTime: string;
  price: number;
  availableSeats: number;
  totalSeats: number;
  active: boolean;
  soldTickets?: number;
  createdAt?: string;
}

export interface HallResponse {
  id: number;
  name: string;
  capacity: number;
  description?: string;
}

export type SeatType = 'REGULAR' | 'VIP' | 'COUPLE' | string;

export interface SeatResponse {
  id: number;
  hallId: number;
  hallName: string;
  rowNumber: string;
  seatNumber: number;
  type: SeatType;
  reserved: boolean;
}

export interface CreateOrderItemRequest {
  screeningId: number;
  seatId: number;
  quantity: number;
}

export interface CreateOrderRequest {
  userId: number;
  orderItems: CreateOrderItemRequest[];
}

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED' | string;

export interface OrderItemResponse {
  id: number;
  screeningId: number;
  movieTitle: string;
  hallName: string;
  screeningDateTime: string;
  seatId: number;
  seatInfo: string;
  quantity: number;
  price: number;
  subtotal: number;
}

export interface OrderResponse {
  id: number;
  userId: number;
  username: string;
  totalPrice: number;
  orderDate: string;
  status: OrderStatus;
  paymentIntentId?: string;
  orderItems: OrderItemResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface StripeCheckoutResponse {
  sessionId: string;
  url: string;
  message: string;
}

export interface AdminPaymentItemResponse {
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface AdminPaymentTransactionResponse {
  paymentId: number;
  paymentIntentId: string;
  paymentStatus: string;
  paymentMethod?: string;
  paidAt?: string;
  orderId: number;
  orderStatus: string;
  totalAmount: number;
  currency: string;
  customerId: number;
  customerUsername: string;
  customerEmail: string;
  purchasedItems: AdminPaymentItemResponse[];
}

export interface PaymentResponse {
  id: number;
  orderId: number;
  paymentIntentId?: string;
  amount: number;
  currency: string;
  status: string;
  paymentMethod?: string;
  paidAt?: string;
  createdAt?: string;
  updatedAt?: string;
}
