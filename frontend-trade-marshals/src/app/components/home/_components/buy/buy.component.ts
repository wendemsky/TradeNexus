import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Price } from 'src/app/models/price';
import { TradingFormComponent } from '../trading-form/trading-form.component';
import { PriceService } from 'src/app/services/price.service';

@Component({
  selector: 'app-buy',
  templateUrl: './buy.component.html',
  styleUrls: ['./buy.component.css']
})
export class BuyComponent {
  prices: Price[] = [];

  constructor(
    private priceService: PriceService,
    private _dialog: MatDialog
  ) {}

   ngOnInit(): void {
    this.loadAllPrices();
  }

  loadAllPrices() {
    this.priceService.getPrices()
      .subscribe(data => this.prices = data);
  }

  params: any;
  agInit(params: any): void {
    this.params = params;
  }

  onClickBuy(price: Price) {
    console.log('Buy', price.instrument);
    const tradeFormData = {
      askPrice: price.askPrice,
      bidPrice: price.bidPrice,
      priceTimeStamp: price.priceTimestamp,
      direction: 'B',
      instrument: price.instrument
    }
    this._dialog.open(TradingFormComponent, {
      data: tradeFormData
    });
  }
}
