import { Component } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';

@Component({
  selector: 'tn-price-flash-cell',
  standalone: true,
  template: `<span [class]="flashClass" class="price-cell block">{{ formatted }}</span>`,
})
export class PriceFlashCellComponent implements ICellRendererAngularComp {
  formatted  = '';
  flashClass = '';

  private prevValue: number | null = null;
  private flashTimer?: ReturnType<typeof setTimeout>;

  agInit(params: ICellRendererParams<unknown, number>): void {
    this.updateValue(params.value ?? 0);
  }

  refresh(params: ICellRendererParams<unknown, number>): boolean {
    this.updateValue(params.value ?? 0);
    return true;
  }

  private updateValue(value: number): void {
    if (this.prevValue !== null && value !== this.prevValue) {
      clearTimeout(this.flashTimer);
      this.flashClass = value > this.prevValue ? 'price-flash-up' : 'price-flash-down';
      this.flashTimer = setTimeout(() => (this.flashClass = ''), 800);
    }
    this.prevValue = value;
    this.formatted = value.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
  }
}
