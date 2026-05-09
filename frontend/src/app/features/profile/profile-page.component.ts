import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { BookingService } from '../../core/services/booking.service';
import { OrderResponse } from '../../core/models/booking.models';
import { EditProfileDialogComponent } from './edit-profile-dialog.component';

@Component({
  selector: 'app-profile-page',
  imports: [
    CommonModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDividerModule,
    MatIconModule,
    DatePipe,
    CurrencyPipe
  ],
  templateUrl: './profile-page.component.html',
  styleUrls: ['./profile-page.component.scss'],
 
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfilePageComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly bookingService = inject(BookingService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly currentUser = this.authService.currentUser;
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly orders = signal<OrderResponse[]>([]);

  protected readonly orderCount = computed(() => this.orders().length);
  protected readonly totalTickets = computed(() =>
    this.orders().reduce((count, order) => count + order.orderItems.length, 0)
  );
  protected readonly totalSpent = computed(() =>
    this.orders().reduce((sum, order) => sum + Number(order.totalPrice || 0), 0)
  );
  protected readonly latestOrderDate = computed(() => {
    const latest = [...this.orders()].sort((left, right) => new Date(right.orderDate).getTime() - new Date(left.orderDate).getTime())[0];
    return latest ? new Date(latest.orderDate).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }) : '';
  });

  ngOnInit() {
    this.loadProfileData();
  }

  private loadProfileData() {
    const user = this.currentUser();

    if (!user) {
      this.loading.set(false);
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.bookingService.getOrdersByUserId(user.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (orders) => {
          const sortedOrders = [...orders].sort((left, right) => new Date(right.orderDate).getTime() - new Date(left.orderDate).getTime());
          this.orders.set(sortedOrders);
          this.loading.set(false);
        },
        error: () => {
          this.orders.set([]);
          this.errorMessage.set('Ticket history is temporarily unavailable. Please try again later.');
          this.loading.set(false);
        }
      });
  }

openEditDialog() {
  const dialogRef = this.dialog.open(EditProfileDialogComponent, {
    width: '620px',
    maxWidth: '92vw',
    maxHeight: '90vh',
    disableClose: false,
    panelClass: 'edit-profile-dialog-panel',
    backdropClass: 'edit-profile-backdrop'
  });

  dialogRef.afterClosed().subscribe((result) => {
    if (result) {
      this.loadProfileData();
    }
  });
}
}
