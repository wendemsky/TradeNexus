import { Component, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ClientService } from '../../../core/services/client.service';
import { PriceStore } from '../../../store/price.store';
import { authStore } from '../../../store/auth.store';
import { passwordStrengthValidator, passwordMatchValidator } from '../../../shared/validators/password.validator';
import { idFormatValidator } from '../../../shared/validators/id.validator';
import { environment } from '../../../../environments/environment';

type Step = 1 | 2 | 3;

@Component({
  selector: 'tn-registration',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registration.component.html',
})
export class RegistrationComponent {
  private readonly fb         = inject(FormBuilder);
  private readonly router     = inject(Router);
  private readonly client     = inject(ClientService);
  private readonly priceStore = inject(PriceStore);

  readonly currentStep = signal<Step>(1);
  readonly isLoading   = signal(false);
  readonly errorMsg    = signal('');

  readonly credentialsForm = this.fb.group(
    {
      email:           ['', [Validators.required, Validators.email]],
      password:        ['', [Validators.required, Validators.minLength(8), passwordStrengthValidator()]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordMatchValidator('password', 'confirmPassword') }
  );

  readonly personalForm = this.fb.group({
    name:        ['', [Validators.required, Validators.minLength(2)]],
    dateOfBirth: ['', Validators.required],
    country:     ['India' as 'India' | 'USA', Validators.required],
  });

  readonly idForm = this.fb.group({
    idType:  ['Aadhar', Validators.required],
    idValue: ['', [Validators.required]],
  });

  readonly maxDob = computed(() => {
    const d = new Date();
    d.setFullYear(d.getFullYear() - 18);
    return d.toISOString().split('T')[0];
  });

  readonly idTypes = computed(() => {
    const country = this.personalForm.get('country')?.value;
    return country === 'India' ? ['Aadhar', 'PAN'] : ['SSN'];
  });

  ngOnInit(): void {
    this.idForm.get('idType')!.valueChanges.subscribe(type => {
      if (type) {
        const ctrl = this.idForm.get('idValue')!;
        ctrl.setValue('');
        ctrl.setValidators([Validators.required, idFormatValidator(type)]);
        ctrl.updateValueAndValidity();
      }
    });

    this.personalForm.get('country')!.valueChanges.subscribe(() => {
      const defaultType = this.idTypes()[0];
      this.idForm.get('idType')!.setValue(defaultType);
    });

    this.idForm.get('idType')!.setValue('Aadhar');
  }

  goToStep(step: Step): void {
    this.currentStep.set(step);
    this.errorMsg.set('');
  }

  nextFromStep1(): void {
    if (this.credentialsForm.invalid) {
      this.credentialsForm.markAllAsTouched();
      return;
    }
    this.isLoading.set(true);
    this.errorMsg.set('');

    const email = this.credentialsForm.value.email!;
    this.client.verifyEmail(email).subscribe({
      next: ({ isVerified }) => {
        if (isVerified) {
          this.errorMsg.set('An account with this email already exists.');
        } else {
          this.goToStep(2);
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMsg.set('Unable to verify email. Please try again.');
        this.isLoading.set(false);
      },
    });
  }

  nextFromStep2(): void {
    if (this.personalForm.invalid) {
      this.personalForm.markAllAsTouched();
      return;
    }
    this.goToStep(3);
  }

  submit(): void {
    if (this.idForm.invalid) {
      this.idForm.markAllAsTouched();
      return;
    }
    this.isLoading.set(true);
    this.errorMsg.set('');

    const { email, password } = this.credentialsForm.value;
    const { name, dateOfBirth, country } = this.personalForm.value;
    const { idType, idValue } = this.idForm.value;

    this.client.register({
      email:          email!,
      password:       password!,
      name:           name!,
      dateOfBirth:    dateOfBirth!,
      country:        country!,
      identification: [{ type: idType!, value: idValue! }],
    }).subscribe({
      next: profile => {
        authStore.login(profile);
        this.priceStore.connect(environment.wsUrl, profile.token);
        this.router.navigate(['/dashboard/preferences']);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMsg.set(err.error?.message ?? 'Registration failed. Please try again.');
        this.isLoading.set(false);
      },
    });
  }
}
