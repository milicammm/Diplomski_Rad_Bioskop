import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { RegisterRequest, UserResponse } from '../../core/models/auth.models';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-edit-profile-dialog',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
    MatIconModule
  ],
    templateUrl: './edit-profile-dialog.component.html',
    styleUrls: ['./edit-profile-dialog.component.scss'],
})
export class EditProfileDialogComponent {
  private readonly authService = inject(AuthService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  readonly dialogRef = inject(MatDialogRef<EditProfileDialogComponent>);

  protected readonly hidePassword = signal(true);
  protected readonly isSubmitting = signal(false);

  protected form = this.formBuilder.group({
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    phoneNumber: [''],
    password: ['', [Validators.required, Validators.minLength(6)]],
    username: [''] 
  });

  ngOnInit() {
    const currentUser = this.authService.currentUser();
    if (currentUser) {
      this.form.patchValue({
        firstName: currentUser.firstName,
        lastName: currentUser.lastName,
        email: currentUser.email,
        phoneNumber: currentUser.phoneNumber || '',
        username: currentUser.username
      });
      this.form.get('username')?.disable();
    }
  }

  submit() {
    if (this.form.invalid) {
      return;
    }

    this.isSubmitting.set(true);

    const formValue = this.form.getRawValue();
    const payload: RegisterRequest = {
      username: formValue.username || '',
      email: formValue.email || '',
      password: formValue.password || '',
      firstName: formValue.firstName || '',
      lastName: formValue.lastName || '',
      phoneNumber: formValue.phoneNumber || undefined
    };

    this.authService.updateProfile(payload).subscribe({
      next: (response: UserResponse) => {
        this.isSubmitting.set(false);
        this.snackBar.open('Profile updated successfully!', 'Close', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
        this.dialogRef.close(response);
      },
      error: (error) => {
        this.isSubmitting.set(false);
        let errorMessage = 'Failed to update profile. Please try again.';

        if (error.error?.message) {
          errorMessage = error.error.message;
        } else if (error.status === 409) {
          errorMessage = error.error?.message?.includes('email')
            ? 'This email is already in use. Please use a different email.'
            : 'This username is already taken. Please choose a different one.';
        } else if (error.status === 401) {
          errorMessage = 'Your session has expired. Please log in again.';
        }

        this.snackBar.open(errorMessage, 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
