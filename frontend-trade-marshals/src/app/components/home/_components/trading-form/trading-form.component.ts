import { HttpClient } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { Instrument } from 'src/app/models/instrument';
import { Order } from 'src/app/models/order';
import { Trade } from 'src/app/models/trade';
import { mockTrades } from 'src/assets/mock-data/mock-trade-history';

@Component({
  selector: 'app-trading-form',
  templateUrl: './trading-form.component.html',
  styleUrls: ['./trading-form.component.css']
})
export class TradingFormComponent implements OnInit{
  askPrice: number;
  bidPrice: number;
  instrument: Instrument;
  direction: string;
  priceTimeStamp: string;
  order: Order = new Order('', -1, -1, '', '', '', -1);
  trade?: Trade;

  constructor(
    private httpClient: HttpClient,
    @Inject(MAT_DIALOG_DATA) public data: any
  ){
    this.askPrice = data.askPrice;
    this.bidPrice = data.bidPrice;
    this.instrument = data.instrument;
    this.direction = data.direction;
    this.priceTimeStamp = data.priceTimeStamp;

    console.log(data);
  }

  ngOnInit(): void {
    this.order.clientId = '920265077';
    this.order.token = 920141621;
    this.order.direction = this.direction;
    this.order.quantity = this.instrument.minQuantity;
    this.order.instrumentId = this.instrument.instrumentId;
    this.order.targetPrice = this.direction === 'B' ? this.bidPrice : this.askPrice;

    console.log('Order', this.order);
  }

  executeTrade(order: Order) {
    const url = 'http://localhost:3000/fmts/trades/trade';

    this.httpClient.post<Trade>(url, order)
      .subscribe(data => {
        this.trade = data;
        console.log('Trade Executed', this.trade);
        // Populate trade history, portfolio etc.
        mockTrades.push(this.trade);
      });
  }
}
 