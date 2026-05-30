import { FormControl } from '@angular/forms';
import { idFormatValidator } from './id.validator';

describe('idFormatValidator', () => {
  describe('Aadhar', () => {
    const validate = idFormatValidator('Aadhar');

    it('accepts exactly 12 digits', () => {
      expect(validate(new FormControl('123456789012'))).toBeNull();
    });

    it('rejects fewer than 12 digits', () => {
      expect(validate(new FormControl('12345'))).toEqual({ invalidIdFormat: { idType: 'Aadhar' } });
    });

    it('rejects letters', () => {
      expect(validate(new FormControl('12345678901A'))).toEqual({ invalidIdFormat: { idType: 'Aadhar' } });
    });
  });

  describe('PAN', () => {
    const validate = idFormatValidator('PAN');

    it('accepts valid PAN format AAAAA9999A', () => {
      expect(validate(new FormControl('ABCDE1234F'))).toBeNull();
    });

    it('rejects lowercase PAN', () => {
      expect(validate(new FormControl('abcde1234f'))).toEqual({ invalidIdFormat: { idType: 'PAN' } });
    });

    it('rejects wrong digit count', () => {
      expect(validate(new FormControl('ABCDE12345'))).toEqual({ invalidIdFormat: { idType: 'PAN' } });
    });
  });

  describe('SSN', () => {
    const validate = idFormatValidator('SSN');

    it('accepts exactly 9 digits', () => {
      expect(validate(new FormControl('123456789'))).toBeNull();
    });

    it('rejects SSN with a letter', () => {
      expect(validate(new FormControl('12345678A'))).toEqual({ invalidIdFormat: { idType: 'SSN' } });
    });

    it('rejects 8-digit SSN', () => {
      expect(validate(new FormControl('12345678'))).toEqual({ invalidIdFormat: { idType: 'SSN' } });
    });
  });

  it('returns null for empty value regardless of type', () => {
    expect(idFormatValidator('Aadhar')(new FormControl(''))).toBeNull();
    expect(idFormatValidator('PAN')(new FormControl(''))).toBeNull();
    expect(idFormatValidator('SSN')(new FormControl(''))).toBeNull();
  });

  it('returns null for unknown ID type', () => {
    expect(idFormatValidator('PASSPORT')(new FormControl('anything'))).toBeNull();
  });
});
