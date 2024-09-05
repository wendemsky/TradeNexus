import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Price } from 'src/app/models/price';
import { TradingFormComponent } from '../trading-form/trading-form.component';
import { PriceService } from 'src/app/services/price.service';

@Component({
  selector: 'app-sell',
  templateUrl: './sell.component.html',
  styleUrls: ['./sell.component.css']
})
export class SellComponent implements OnInit {
  prices: Price[] = [];
  price?: Price;

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
  
  onClickSell(price: Price) {
    console.log('Sell', price.instrument);
    const tradeFormData = {
      askPrice: price.askPrice,
      bidPrice: price.bidPrice,
      priceTimeStamp: price.priceTimestamp,
      direction: 'S',
      instrument: price.instrument
    }

    this._dialog.open(TradingFormComponent, {
      width: '50vw',
      data: tradeFormData
    });
  }
}
