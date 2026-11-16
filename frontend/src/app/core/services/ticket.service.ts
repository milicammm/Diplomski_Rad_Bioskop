import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export interface TicketInfo {
  id: number;
  ticketNumber: string;
  emailSent: boolean;
  createdAt: string;
  qrCode: string;
}

@Injectable({ providedIn: 'root' })
export class TicketService {
  private readonly http = inject(HttpClient);

  /**
   * Download ticket PDF for an order
   */
  downloadTicket(orderId: number): Observable<Blob> {
    return this.http.get(`${API_BASE_URL}/api/tickets/${orderId}`, {
      responseType: 'blob'
    });
  }

  /**
   * Get ticket information (metadata) for an order
   */
  getTicketInfo(orderId: number): Observable<TicketInfo> {
    return this.http.get<TicketInfo>(`${API_BASE_URL}/api/tickets/${orderId}/info`);
  }

  /**
   * Resend ticket email for an order
   */
  resendTicketEmail(orderId: number): Observable<{ message: string; orderId: number }> {
    return this.http.post<{ message: string; orderId: number }>(
      `${API_BASE_URL}/api/tickets/${orderId}/resend-email`,
      {}
    );
  }

  /**
   * Trigger file download in browser and return observable
   */
  triggerDownload(blob: Blob, fileName: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.click();
    window.URL.revokeObjectURL(url);
  }
}
