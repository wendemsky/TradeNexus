import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

const ID_PATTERNS: Record<string, RegExp> = {
  Aadhar: /^\d{12}$/,
  PAN:    /^[A-Z]{5}\d{4}[A-Z]$/,
  SSN:    /^\d{9}$/,
};

export function idFormatValidator(idType: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;
    if (!value || !idType) return null;

    const pattern = ID_PATTERNS[idType];
    if (!pattern) return null;

    return pattern.test(value) ? null : { invalidIdFormat: { idType } };
  };
}
