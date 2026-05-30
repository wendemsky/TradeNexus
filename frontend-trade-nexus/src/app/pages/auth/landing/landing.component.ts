import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ClientService } from '../../../core/services/client.service';
import { PriceStore } from '../../../store/price.store';
import { authStore } from '../../../store/auth.store';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'tn-landing',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './landing.component.html',
})
export class LandingComponent {
  private readonly fb        = inject(FormBuilder);
  private readonly router    = inject(Router);
  private readonly client    = inject(ClientService);
  private readonly priceStore = inject(PriceStore);

  readonly isLoading = signal(false);
  readonly errorMsg  = signal('');

  readonly form = this.fb.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  onSubmit(): void {
    if (this.form.invalid || this.isLoading()) return;
    this.isLoading.set(true);
    this.errorMsg.set('');

    const { email, password } = this.form.value;
    this.client.login({ email: email!, password: password! }).subscribe({
      next: profile => {
        authStore.login(profile);
        this.priceStore.connect(environment.wsUrl, profile.token);
        this.router.navigate(['/dashboard']);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMsg.set(
          err.status === 401 ? 'Invalid email or password.' : 'Login failed. Please try again.'
        );
        this.isLoading.set(false);
      },
    });
  }
}
