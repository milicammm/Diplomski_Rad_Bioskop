import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { guestGuard } from './core/guards/guest.guard';
import { notAdminGuard } from './core/guards/not-admin.guard';
import { MainShellComponent } from './shared/layout/main-shell.component';

export const routes: Routes = [
	{
		path: '',
		component: MainShellComponent,
		children: [
			{
				path: '',
				pathMatch: 'full',
				redirectTo: 'home'
			},
			{
				path: 'home',
				loadComponent: () => import('./features/home/home-page.component').then((m) => m.HomePageComponent)
			},
			{
				path: 'movies',
				loadComponent: () => import('./features/movies/movies-page.component').then((m: any) => m.MoviesPageComponent),
				canActivate: [notAdminGuard]
			},
			{
				path: 'tickets',
				loadComponent: () => import('./features/tickets/tickets-page.component').then((m) => m.TicketsPageComponent),
				canActivate: [notAdminGuard]
			},
			{
				path: 'about',
				loadComponent: () => import('./features/about/about-page.component').then((m) => m.AboutPageComponent)
			},
			{
				path: 'profile',
				loadComponent: () => import('./features/profile/profile-page.component').then((m) => m.ProfilePageComponent),
				canActivate: [authGuard]
			},
			{
				path: 'checkout',
				loadComponent: () => import('./features/checkout/checkout-page.component').then((m) => m.CheckoutPageComponent),
				canActivate: [authGuard, notAdminGuard]
			},
			{
				path: 'admin',
				loadComponent: () => import('./features/admin/admin-dashboard-page.component').then((m) => m.AdminDashboardPageComponent),
				canActivate: [adminGuard]
			},
			{
				path: 'payment/success',
				loadComponent: () => import('./features/payment/payment-sucess-page.component').then((m) => m.PaymentSuccessPageComponent),
				canActivate: [authGuard, notAdminGuard]
			},
			{
				path: 'payment/cancel',
				loadComponent: () => import('./features/payment/payment-cancel-page.component').then((m) => m.PaymentCancelPageComponent),
				canActivate: [authGuard, notAdminGuard]
			}
		]
	},
	{
		path: 'auth/login',
		loadComponent: () => import('./features/auth/login-page.component').then((m) => m.LoginPageComponent),
		canActivate: [guestGuard]
	},
	{
		path: 'auth/register',
		loadComponent: () => import('./features/auth/register-page.component').then((m) => m.RegisterPageComponent),
		canActivate: [guestGuard]
	},
	{
		path: '**',
		redirectTo: 'home'
	}
];
