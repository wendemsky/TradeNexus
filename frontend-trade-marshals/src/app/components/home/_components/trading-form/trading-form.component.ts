import { Component, inject, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { ClientPortfolio } from 'src/app/models/Client/ClientPortfolio';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';
import { Holding } from 'src/app/models/Trade/Holding';
import { Instrument } from 'src/app/models/Trade/instrument';
import { Order } from 'src/app/models/Trade/order';
import { Trade } from 'src/app/models/Trade/trade';
import { ClientPortfolioService } from 'src/app/services/Client/client-portfolio.service';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';
import { TradeHistoryService } from 'src/app/services/Trade/trade-history.service';
import { TradeService } from 'src/app/services/Trade/trade.service';
import { v4 as uuidv4 } from 'uuid';
import { Router } from '@angular/router';



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
  clientId: string | undefined;
  token: number | undefined;
  orderId = uuidv4();
  lowerLimit!: number;
  upperLimit!: number;
  withinOnePercentOfAskOrBidPrice?: boolean;
  public _snackBar = inject(MatSnackBar);
  
  

  clientProfileData!: ClientProfile | null; //Client Profile data that is set with ClientProfileService
  clientPortfolioData!: ClientPortfolio;
  
  errorMessage: string = '' //For Form Validation

  constructor(
    private tradeService: TradeService,
    private tradeHistoryService: TradeHistoryService,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public clientProfileService: ClientProfileService,
    public clientPortfolioService: ClientPortfolioService,
    private router: Router
  ){
    this.askPrice = data.askPrice;
    this.bidPrice = data.bidPrice;
    this.instrument = data.instrument;
    this.direction = data.direction;
    this.priceTimeStamp = data.priceTimeStamp;

    console.log(data);
  }

  ngOnInit(): void {
    this.clientProfileService.getClientProfile().subscribe(profile => {
      this.clientProfileData = profile;
      console.log(this.clientProfileData);
      
      // console.log('Logged in Client Portfolio Data: ', this.clientPortfolioData);
      
    })
    this.clientId = this.clientProfileData?.client?.clientId;
    this.clientId !== undefined ? this.order.clientId = this.clientId : console.error('Client ID is undefined');
    
    this.token = this.clientProfileData?.token;
    this.token !== undefined ? this.order.token = this.token : console.error('Token in of type undefined');

    this.order.direction = this.direction;
    this.order.quantity = this.instrument?.minQuantity;
    this.order.instrumentId = this.instrument?.instrumentId;
    this.order.targetPrice = this.direction === 'B' ? this.bidPrice : this.askPrice;

    this.order.orderId = this.orderId;

    // console.log('Order', this.order);

    this.upperLimit = this.order.direction === 'B' ? (this.bidPrice + (this.bidPrice/100)) : (this.askPrice + (this.askPrice/100));
    this.lowerLimit = this.order.direction === 'B' ? (this.bidPrice - (this.bidPrice/100)) : (this.askPrice - (this.askPrice/100));
    
  }

  executeTrade(order: Order) {
    console.log('Order: ', order);
    this.tradeService.addOrder(order)
      .subscribe({
        next:   (data) => {
          this.trade = data;
          console.log('Trade Executed: ', this.trade);
          // Populate trade history, portfolio etc.
          if(this.trade === null) {
            console.error('Trade Execution Error');
            this._snackBar.open('Trade Execution Error', '', {
              duration: 3000
            });
          } else {
            this._snackBar.open("Order placed successfully", '', {
              duration: 3000,
              panelClass: ['form-submit-snackbar']
            })      
          }
        },
        error: (e) => {
          console.error('Trade Execution error: ',e)
          this._snackBar.open(e, '', {
            duration: 3000,
            panelClass: ['red-snackbar']
          })
        }
      });
  }

  tradeSaved?: Trade;
  saveTrade(trade: Trade) { 
    this.tradeHistoryService.addTrade(trade).pipe()
      .subscribe(data => {
        this.tradeSaved = data;
        console.log(this.tradeSaved);
        // this._snackBar.open('Trade saved successfully', '', {
        //   duration: 3000,
        //   panelClass: ['form-submit-snackbar']
        // });
      }); 
  }

}

  



 