import { FormControl, FormGroup } from '@angular/forms';
import { passwordMatchValidator, passwordStrengthValidator } from './password.validator';

describe('passwordStrengthValidator', () => {
  const validate = passwordStrengthValidator();

  it('returns null for a strong password', () => {
    expect(validate(new FormControl('Str0ngPass'))).toBeNull();
  });

  it('returns null for empty value', () => {
    expect(validate(new FormControl(''))).toBeNull();
  });

  it('fails when missing uppercase', () => {
    const result = validate(new FormControl('weak1pass'));
    expect(result).toEqual({ passwordStrength: { hasUppercase: false, hasLowercase: true, hasDigit: true } });
  });

  it('fails when missing lowercase', () => {
    const result = validate(new FormControl('WEAK1PASS'));
    expect(result).toEqual({ passwordStrength: { hasUppercase: true, hasLowercase: false, hasDigit: true } });
  });

  it('fails when missing digit', () => {
    const result = validate(new FormControl('WeakPassword'));
    expect(result).toEqual({ passwordStrength: { hasUppercase: true, hasLowercase: true, hasDigit: false } });
  });

  it('fails when all three rules are broken', () => {
    const result = validate(new FormControl('nouppernodigit'));
    expect(result?.['passwordStrength'].hasUppercase).toBe(false);
    expect(result?.['passwordStrength'].hasDigit).toBe(false);
  });
});

describe('passwordMatchValidator', () => {
  const validate = passwordMatchValidator('password', 'confirmPassword');

  it('returns null when passwords match', () => {
    const group = new FormGroup({
      password:        new FormControl('Test1234'),
      confirmPassword: new FormControl('Test1234'),
    });
    expect(validate(group)).toBeNull();
  });

  it('returns passwordMismatch when passwords differ', () => {
    const group = new FormGroup({
      password:        new FormControl('Test1234'),
      confirmPassword: new FormControl('Different1'),
    });
    expect(validate(group)).toEqual({ passwordMismatch: true });
  });
});
