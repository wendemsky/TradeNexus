import {
  Component, computed, inject, OnInit, signal
} from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { toObservable } from '@angular/core/rxjs-interop';
import { PriceStore } from '../../../store/price.store';
import { authStore } from '../../../store/auth.store';
import { TradeService } from '../../../core/services/trade.service';
import { Price } from '../../../shared/models/price.models';
import { Order } from '../../../shared/models/trade.models';

export interface TradingFormDialogData {
  price: Price;
  direction: 'B' | 'S';
}

@Component({
  selector: 'tn-trading-form-dialog',
  standalone: true,
  imports: [ReactiveFormsModule, DecimalPipe],
  templateUrl: './trading-form-dialog.component.html',
})
export class TradingFormDialogComponent implements OnInit {
  readonly data       = inject<TradingFormDialogData>(DIALOG_DATA);
  private readonly dialogRef   = inject(DialogRef<boolean>);
  private readonly fb          = inject(FormBuilder);
  private readonly tradeService = inject(TradeService);
  private readonly priceStore  = inject(PriceStore);

  readonly isLoading = signal(false);
  readonly errorMsg  = signal('');
  readonly orderType = signal<'MARKET' | 'LIMIT'>('MARKET');

  readonly livePrice = computed(() =>
    this.priceStore.prices().get(this.data.price.instrumentId) ?? this.data.price
  );

  readonly currentAsk = computed(() => this.livePrice().askPrice);
  readonly currentBid = computed(() => this.livePrice().bidPrice);

  readonly form = this.fb.group({
    quantity:    [1, [Validators.required, Validators.min(1)]],
    targetPrice: [0, [Validators.required, Validators.min(0.01)]],
  });

  readonly instrument = this.data.price.instrument;
  readonly isBuy      = this.data.direction === 'B';

  readonly qty = computed(() => {
    const raw = this.form.get('quantity')?.value ?? 0;
    return typeof raw === 'number' ? raw : 0;
  });

  readonly feeAmount = computed(() => {
    const price = this.isBuy ? this.currentAsk() : this.currentBid();
    return this.qty() * price * 0.001;
  });

  readonly totalCost = computed(() => {
    const price = this.isBuy ? this.currentAsk() : this.currentBid();
    const factor = this.isBuy ? 1.001 : 0.999;
    return this.qty() * price * factor;
  });

  ngOnInit(): void {
    this.form.patchValue({
      quantity:    this.instrument.minQuantity,
      targetPrice: this.isBuy ? this.currentAsk() : this.currentBid(),
    });

    this.form.get('quantity')!.setValidators([
      Validators.required,
      Validators.min(this.instrument.minQuantity),
      Validators.max(this.instrument.maxQuantity),
    ]);
    this.form.get('quantity')!.updateValueAndValidity();
  }

  setOrderType(type: 'MARKET' | 'LIMIT'): void {
    this.orderType.set(type);
    if (type === 'MARKET') {
      this.form.get('targetPrice')!.clearValidators();
    } else {
      this.form.get('targetPrice')!.setValidators([Validators.required, Validators.min(0.01)]);
      this.form.patchValue({
        targetPrice: this.isBuy ? this.currentAsk() : this.currentBid(),
      });
    }
    this.form.get('targetPrice')!.updateValueAndValidity();
  }

  close(): void {
    this.dialogRef.close(false);
  }

  submit(): void {
    if (this.form.invalid || this.isLoading()) return;
    this.isLoading.set(true);
    this.errorMsg.set('');

    const order: Order = {
      orderId:     crypto.randomUUID(),
      instrumentId: this.data.price.instrumentId,
      quantity:    this.form.value.quantity!,
      targetPrice: this.orderType() === 'LIMIT' ? this.form.value.targetPrice! : null,
      direction:   this.data.direction,
      orderType:   this.orderType(),
      clientId:    authStore.clientId()!,
      token:       authStore.profile()!.token,
    };

    this.tradeService.executeTrade(order).subscribe({
      next: () => {
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        const code = err.error?.errorCode ?? err.error?.message ?? 'TRADE_FAILED';
        this.errorMsg.set(this.friendlyError(code));
        this.isLoading.set(false);
      },
    });
  }

  private friendlyError(code: string): string {
    const map: Record<string, string> = {
      MARKET_CLOSED:        'Market is currently closed. Use a LIMIT order or try later.',
      PRICE_DATA_STALE:     'Price data is stale. Please wait a moment and retry.',
      LIMIT_NOT_MET:        'Limit price not met at current market price.',
      INSUFFICIENT_BALANCE: 'Insufficient cash balance for this trade.',
      INSUFFICIENT_HOLDINGS:'You don\'t hold enough of this instrument to sell.',
      ORDER_QTY_BELOW_MIN:  `Minimum quantity is ${this.instrument.minQuantity}.`,
      ORDER_QTY_ABOVE_MAX:  `Maximum quantity is ${this.instrument.maxQuantity}.`,
    };
    return map[code] ?? `Trade failed: ${code}`;
  }
}
