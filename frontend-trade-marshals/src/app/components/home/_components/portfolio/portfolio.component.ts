import { Component, OnInit } from '@angular/core';
import { ColDef, SideBarDef } from 'ag-grid-community';
import { BuyComponent } from '../buy/buy.component';
import { SellComponent } from '../sell/sell.component';
import { ClientPortfolioService } from 'src/app/services/Client/client-portfolio.service';
import { ClientPortfolio } from 'src/app/models/Client/ClientPortfolio';


@Component({
  selector: 'app-portfolio',
  templateUrl: './portfolio.component.html',
  styleUrls: ['./portfolio.component.css']
})
export class PortfolioComponent implements OnInit{

  clientId: string = '739982664';

  portfolioData: ClientPortfolio[] = [];
  transformedData: any[] = [];
  currentBalance: any;

  public columnDefs: ColDef[] = [
    {
      headerName: "Instrument Description", 
      field: "instrumentDesc",
      minWidth: 400,
    },
    { 
      headerName: "Instrument ID", 
      field: "instrumentId",
    },
    { 
      headerName: "Category ID", 
      field: "categoryId",
    },
    { 
      headerName: "Quantity", 
      field: "quantity",
    }, 
    { 
      headerName: "Average Price", 
      field: "avgPrice",
    }, 
    { 
      headerName: "Buy", 
      field: "buy",
      cellRenderer: BuyComponent
    },
    { 
      headerName: "Sell", 
      field: "sell",
      cellRenderer: SellComponent
    }
  ]

  public defaultColDef: ColDef = {
    flex: 1,
    resizable: true,
    sortable: true,
    filter: true,
  }

  public sidebar: SideBarDef | null = {
    toolPanels: [
      {
          id: 'columns',
          labelDefault: 'Columns',
          labelKey: 'columns',
          iconKey: 'columns',
          toolPanel: 'agColumnsToolPanel',
      },
      {
          id: 'filters',
          labelDefault: 'Filters',
          labelKey: 'filters',
          iconKey: 'filter',
          toolPanel: 'agFiltersToolPanel',
      }
  ],    
    position: 'left',
  }
  
  constructor(private clientPortfolioService: ClientPortfolioService){
    
  }

  ngOnInit(): void{
    this.loadPortfolio();
  }

  loadPortfolio(){
    this.clientPortfolioService.getClientPortfolio(this.clientId)
    .subscribe( (data) => {
      this.portfolioData = data;
      this.transformData();
    })
  }

  // Function to flatten the holdings array into individual rows
  transformData(){
     this.transformedData = this.portfolioData.flatMap(portfolio =>
      portfolio.holdings.map(holding => ({
        instrumentId: holding.instrumentId,
        categoryId: holding.categoryId,
        instrumentDesc: holding.instrumentDesc,
        quantity: holding.quantity,
        avgPrice: holding.avgPrice
      }),
      this.currentBalance = portfolio.currBalance
    )
    )
  }
  

}
