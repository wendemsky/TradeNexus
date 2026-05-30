import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function passwordStrengthValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;
    if (!value) return null;

    const hasUppercase = /[A-Z]/.test(value);
    const hasLowercase = /[a-z]/.test(value);
    const hasDigit     = /\d/.test(value);

    if (hasUppercase && hasLowercase && hasDigit) return null;

    return {
      passwordStrength: {
        hasUppercase,
        hasLowercase,
        hasDigit,
      },
    };
  };
}

export function passwordMatchValidator(passwordKey: string, confirmKey: string): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const password = group.get(passwordKey)?.value as string;
    const confirm  = group.get(confirmKey)?.value as string;
    return password === confirm ? null : { passwordMismatch: true };
  };
}
