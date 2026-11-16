import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-main-shell',
  imports: [
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    RouterLink,
    RouterOutlet
  ],
  templateUrl: './main-shell.component.html',
  styleUrls: ['./main-shell.component.scss'],

  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MainShellComponent {
  private readonly authService = inject(AuthService);

  protected readonly isAuthenticated = computed(() => this.authService.isAuthenticated());
  protected readonly isAdmin = computed(() => this.authService.isAdmin());
  protected readonly currentUser = computed(() => this.authService.currentUser());

  protected logout() {
    this.authService.logout();
  }
}
