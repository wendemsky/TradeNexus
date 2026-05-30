import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Dialog } from '@angular/cdk/dialog';
import { HttpErrorResponse } from '@angular/common/http';
import { PreferencesService } from '../../../core/services/preferences.service';
import { authStore } from '../../../store/auth.store';
import { ClientPreferences } from '../../../shared/models/client.models';
import { RoboAdvisorDialogComponent } from '../../../shared/components/robo-advisor-dialog/robo-advisor-dialog.component';

@Component({
  selector: 'tn-preferences',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './preferences.component.html',
})
export class PreferencesComponent implements OnInit {
  private readonly fb         = inject(FormBuilder);
  private readonly prefsService = inject(PreferencesService);
  private readonly dialog     = inject(Dialog);

  readonly isLoading  = signal(true);
  readonly isSaving   = signal(false);
  readonly successMsg = signal('');
  readonly errorMsg   = signal('');
  private prefsExist  = false;

  readonly form = this.fb.group({
    investmentPurpose:  ['Education' as ClientPreferences['investmentPurpose'], Validators.required],
    incomeCategory:     ['MIG'       as ClientPreferences['incomeCategory'],    Validators.required],
    lengthOfInvestment: ['Medium'    as ClientPreferences['lengthOfInvestment'],Validators.required],
    percentageOfSpend:  ['Tier2'     as ClientPreferences['percentageOfSpend'], Validators.required],
    riskTolerance:      [3,  [Validators.required, Validators.min(1), Validators.max(5)]],
    acceptAdvisor:      [false],
  });

  ngOnInit(): void {
    const clientId = authStore.clientId();
    if (!clientId) { this.isLoading.set(false); return; }

    this.prefsService.getPreferences(clientId).subscribe({
      next: prefs => {
        this.form.patchValue({
          investmentPurpose:  prefs.investmentPurpose,
          incomeCategory:     prefs.incomeCategory,
          lengthOfInvestment: prefs.lengthOfInvestment,
          percentageOfSpend:  prefs.percentageOfSpend,
          riskTolerance:      prefs.riskTolerance,
          acceptAdvisor:      prefs.acceptAdvisor,
        });
        this.prefsExist = true;
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        if (err.status !== 404) {
          this.errorMsg.set('Failed to load preferences.');
        }
        this.isLoading.set(false);
      },
    });
  }

  save(): void {
    if (this.form.invalid || this.isSaving()) return;
    this.isSaving.set(true);
    this.successMsg.set('');
    this.errorMsg.set('');

    const clientId = authStore.clientId()!;
    const prefs: ClientPreferences = {
      clientId,
      investmentPurpose:  this.form.value.investmentPurpose!,
      incomeCategory:     this.form.value.incomeCategory!,
      lengthOfInvestment: this.form.value.lengthOfInvestment!,
      percentageOfSpend:  this.form.value.percentageOfSpend!,
      riskTolerance:      this.form.value.riskTolerance!,
      acceptAdvisor:      this.form.value.acceptAdvisor ?? false,
    };

    const call = this.prefsExist
      ? this.prefsService.updatePreferences(prefs)
      : this.prefsService.createPreferences(prefs);

    call.subscribe({
      next: () => {
        this.prefsExist = true;
        this.successMsg.set('Preferences saved.');
        this.isSaving.set(false);
      },
      error: () => {
        this.errorMsg.set('Failed to save preferences.');
        this.isSaving.set(false);
      },
    });
  }

  openRoboAdvisor(): void {
    const clientId = authStore.clientId()!;
    const prefs: ClientPreferences = {
      clientId,
      investmentPurpose:  this.form.value.investmentPurpose!,
      incomeCategory:     this.form.value.incomeCategory!,
      lengthOfInvestment: this.form.value.lengthOfInvestment!,
      percentageOfSpend:  this.form.value.percentageOfSpend!,
      riskTolerance:      this.form.value.riskTolerance!,
      acceptAdvisor:      true,
    };
    const isMobile = window.innerWidth < 1024;
    this.dialog.open(RoboAdvisorDialogComponent, {
      data: { clientId, preferences: prefs },
      panelClass: isMobile ? 'fullscreen-dialog-panel' : 'dialog-panel',
      width:    isMobile ? '100vw' : '800px',
      height:   isMobile ? '100vh' : '600px',
      maxWidth: '100vw',
    });
  }

  get riskLabel(): string {
    const v = this.form.value.riskTolerance ?? 3;
    return v <= 1 ? 'Very Conservative'
         : v === 2 ? 'Conservative'
         : v === 3 ? 'Moderate'
         : v === 4 ? 'Aggressive'
         : 'Very Aggressive';
  }
}
