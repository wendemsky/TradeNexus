import { HttpClient } from '@angular/common/http';
import { Component, inject, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { ClientPortfolio } from 'src/app/models/Client/ClientPortfolio';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';
import { Holding } from 'src/app/models/Holding';
import { Instrument } from 'src/app/models/instrument';
import { Order } from 'src/app/models/order';
import { Trade } from 'src/app/models/trade';
import { ClientPortfolioService } from 'src/app/services/Client/client-portfolio.service';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';
import { TradeHistoryService } from 'src/app/services/trade-history.service';
import { TradeService } from 'src/app/services/trade.service';
import { v4 as uuidv4 } from 'uuid';




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
  private _snackBar = inject(MatSnackBar);

  
  

  clientProfileData!: ClientProfile | null; //Client Profile data that is set with ClientProfileService
  clientPortfolioData: any[] = [];

  dbJsonTradesUrl = 'http://localhost:4000/trades';
  errorMessage: string = '' //For Form Validation

  constructor(
    private tradeService: TradeService,
    private httpClient: HttpClient,
    private tradeHistoryService: TradeHistoryService,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private clientProfileService: ClientProfileService,
    private clientPortfolioService: ClientPortfolioService,
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

    this.clientId !== undefined ? this.loadClientPortfolioHolding(this.clientId) : console.error('Client ID is undefined');
    
    

  }

  executeTrade(order: Order) {
    const url = 'http://localhost:3000/fmts/trades/trade';
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
            this.addTradeToClientHoldings(this.trade);
          }
        },
        error: (e) => {
          console.error('Trade Execution error: ',e)
          this._snackBar.open(e, '', {
            duration: 3000
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
      }); 
  }

  loadClientPortfolioHolding(clientId: string) {
    this.clientPortfolioService.getClientPortfolio(clientId)
      .subscribe({
        next:   (data) => {
          this.clientPortfolioData = data;
          console.log('Client Portfolio Data: ', this.clientPortfolioData)
          console.log(this.clientPortfolioData[0]);
        }
      })
  }

  holding: Holding = new Holding('', '', '', -1, -1);
  // clientPortfolioUpdateUrl: string = `http://localhost:8000/client-portfolio/`;
  clientPortfolioDataUpdated?: ClientPortfolio;
  addTradeToClientHoldings(trade: Trade) {
    // If holding is already present in the array
    // -> buy -> if currBalance is enough buy -> edit the holding
    // -> sell -> quantity of order.sell should be <= quantity


    // Else holding is not present in the array
    // -> buy -> if currbalance is enough buy
    // -> sell -> X 


    let clientHoldings = this.clientPortfolioData[0].holdings;
    const PresentHolding = clientHoldings.find((holding:any) => holding.instrumentId === this.trade?.instrumentId);
    const indexPresentHolding = clientHoldings.findIndex((holding:any) => holding.instrumentId === this.trade?.instrumentId);
    // Holding exists
    if(PresentHolding) {
      // Buy
      if(this.trade?.direction === 'B'){
        const totalCostOfTrade = (this.trade.executionPrice * this.trade.quantity);
        // You have money to buy
        if(this.clientPortfolioData[0].currBalance >= totalCostOfTrade){
          // Change curr balance
          this.clientPortfolioData[0].currBalance -= totalCostOfTrade;

          // Holding ** check **
          clientHoldings[indexPresentHolding].avgPrice = (clientHoldings[indexPresentHolding].avgPrice + totalCostOfTrade)/(clientHoldings[indexPresentHolding].quantity + this.trade.quantity);
          clientHoldings[indexPresentHolding].quantity += this.trade.quantity;

          
        }
        // You don't have the money to buy
        else {
          console.error('You cant buy with your current balance')
          this._snackBar.open('You cant buy with your current balance', '', {
            duration: 3000
          });
          return;
        }
      }
      // Sell 
      else {
        // You have quantity to sell
        if(this.trade?.quantity && PresentHolding.quantity >= this.trade?.quantity){
          const totalCostOfTrade = (this.trade.executionPrice * this.trade.quantity);
          // Change currentBalance
          this.clientPortfolioData[0].currBalance += totalCostOfTrade;
          // Edit Holding
          clientHoldings[indexPresentHolding].avgPrice = (clientHoldings[indexPresentHolding].avgPrice - totalCostOfTrade)/(clientHoldings[indexPresentHolding].quantity - this.trade.quantity);
          clientHoldings[indexPresentHolding].quantity -= this.trade.quantity;

          // If quantity becomes 0 remove it 
          if(clientHoldings[indexPresentHolding].quantity === 0){
            clientHoldings.splice(indexPresentHolding, 1);
          }
        }
        // You don't have the quantity to sell
        else {
          // snackbar
          console.error('You dont own the quantity to sell');
          this._snackBar.open('You dont own the quantity to sell', '', {
            duration: 3000
          });
          return;
        }
      }
    }
    // Holding doesn't exist 
    else {
      // Buy
      if(this.trade?.direction === 'B'){
        const totalCostOfTrade = (this.trade.executionPrice * this.trade.quantity);
        // You have money to buy
        if(this.clientPortfolioData[0].currBalance >= totalCostOfTrade){
          // Change curr balance
          this.clientPortfolioData[0].currBalance -= totalCostOfTrade;

          // Holding
          this.holding.categoryId = this.instrument.categoryId;
          this.holding.instrumentId = this.trade.instrumentId;
          this.holding.instrumentDesc = this.instrument.instrumentDescription;
          this.holding.quantity = this.trade.quantity;
          this.holding.avgPrice = totalCostOfTrade/this.trade.quantity;

          console.log('Before push');
          // Push holding into the client holding array
          clientHoldings.push(this.holding);
          console.error('Client Holdings after push', clientHoldings);
          this._snackBar.open('Client Holdings after push', '', {
            duration: 3000
          });

        } 
        // You don't have money to buy
        else {
          // snackbar -> you don't have money to buy
          console.error('You dont have the money to buy')
          this._snackBar.open('You dont have the money to buy', '', {
            duration: 3000
          });
          return;
        }
      } else {
        // snackbar -> you don't own the instrument to sell
        console.error('You dont own the instrument to sell')
        this._snackBar.open('You dont own the instrument to sell', '', {
          duration: 3000
        });
        return;
      }
    }


    console.log(clientHoldings);

    
    // Rough
    // We have the client portfolio and holdings, check if the instrument id is already present
    // If present we will edit that holding
    // If not we will push a new entry into the holding array

    /*
    this.holding.categoryId = this.instrument.categoryId;
    // this.holding.instrumentId = this.trade.instrumentId;
    this.holding.instrumentDesc = this.instrument.instrumentDescription;
    this.holding.quantity = this.order.quantity;
    this.trade ? this.holding.avgPrice = this.trade.executionPrice : console.error('Trade property is undefined');


    this.clientPortfolioData[0].holdings ? this.clientPortfolioData[0].holdings.push(this.holding) : console.error('Holding is undefined');
    
    // change currBalance
    this.order.direction === 'B' ? (this.trade ? this.clientPortfolioData[0].currBalance -= (this.trade.quantity * this.trade?.executionPrice) : console.error('Trade is undefined')):
    (this.trade? this.clientPortfolioData[0].currBalance += (this.trade.quantity * this.trade?.executionPrice) : console.error('Trade is undefined'));
    */
    
    console.log('Client Portfolio Data: ', this.clientPortfolioData);
    console.log('Holdings: ', this.clientPortfolioData[0].holdings);

    

    // Everytime its a put request to update

    this.clientPortfolioData[0].holdings = clientHoldings;
    console.log('Client Data before the PUT Req', this.clientPortfolioData[0]);

    this.httpClient.put<ClientPortfolio>(`http://localhost:4000/clients-portfolio/${this.clientPortfolioData[0].id}`, this.clientPortfolioData[0]).pipe()
      .subscribe({
        next: (data) => {
        this.clientPortfolioDataUpdated = data;
        console.log('Updated Client Portfolio', this.clientPortfolioDataUpdated);
        console.log('Updated Client Portfolio Holdings', this.clientPortfolioDataUpdated.holdings);
        
      }, error: (e) => {
        console.error(e);
        this._snackBar.open(e, '', {
          duration: 3000,

        });
      }
    }); 

    this.clientId ? this.loadClientPortfolioHolding(this.clientId) : console.error('Client id is undefined');
    this.saveTrade(this.trade); // Save to trade history
  }

}

  



 