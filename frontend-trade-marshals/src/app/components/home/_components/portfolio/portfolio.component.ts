import { Component, inject, OnInit } from '@angular/core';
import { CellClickedEvent, ColDef, SideBarDef } from 'ag-grid-community';
import { BuyComponent } from '../buy/buy.component';
import { SellComponent } from '../sell/sell.component';
import { ClientPortfolioService } from 'src/app/services/Client/client-portfolio.service';
import { ClientPortfolio } from 'src/app/models/Client/ClientPortfolio';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Instrument } from 'src/app/models/instrument';
import { Holding } from 'src/app/models/Holding';


@Component({
  selector: 'app-portfolio',
  templateUrl: './portfolio.component.html',
  styleUrls: ['./portfolio.component.css']
})
export class PortfolioComponent implements OnInit{

  clientId: string | undefined;

  clientProfileData!: ClientProfile | null;

  portfolioData?: ClientPortfolio;
  transformedData: any[] = [];
  currentBalance: any;
  private _snackBar = inject(MatSnackBar);

  instrumentIdSelected?: string;

  public columnDefs: ColDef[] = [
    // {
    //   headerName: "Instrument Description", 
    //   field: "instrumentDesc",
    //   minWidth: 400,
    // },
    { 
      headerName: "Instrument ID", 
      field: "instrumentId",
    },
    // { 
    //   headerName: "Category ID", 
    //   field: "categoryId",
    // },
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
      cellRenderer: BuyComponent,
      cellRendererParams: {
        instrumentId: this.instrumentIdSelected
      }
    },
    { 
      headerName: "Sell", 
      field: "sell",
      cellRenderer: SellComponent,
      cellRendererParams: {
        instrumentId: this.instrumentIdSelected
      }
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
  
  constructor(
    private clientPortfolioService: ClientPortfolioService,
    private clientProfileService: ClientProfileService
  ){
    
  }

  ngOnInit(): void{
    this.clientProfileService.getClientProfile().subscribe(profile => {
      this.clientProfileData = profile;
      console.log('client ID: ',this.clientProfileData?.client?.clientId);
      this.clientProfileData?.client?.clientId !== undefined ? this.clientId = this.clientProfileData?.client?.clientId : console.error('Client ID is of type undefined');
      this.loadPortfolio();
    })
    
  }

  loadPortfolio(){
    this.clientId ? this.clientPortfolioService.getClientPortfolio(this.clientId)
    .subscribe({
      next: (data) => {
        this.portfolioData = data;
        console.log('Portfolio Data: ', this.portfolioData);
        this.transformData(data);
      }, 
      error: (e) => {
        console.log('Error in loading Trade History: ',e);
        this._snackBar.open(e, '', {
          duration: 3000,
        })
      }
    }) : console.error('Client ID is undefined');
  }

  // Function to flatten the holdings array into individual rows
  transformData(portfolioData: any){
    this.transformedData =  portfolioData.holdings.map((holding: Holding) => ({
      clientId: portfolioData.clientId,
      instrumentId: holding.instrumentId,
      // categoryId: holding.categoryId,
      // instrumentDesc: holding.instrumentDesc,
      quantity: holding.quantity,
      avgPrice: holding.avgPrice
    }));
    this.currentBalance = portfolioData.currBalance
  }

  cellClicked(event: CellClickedEvent) {
    if(event.node.data) {
        console.log("Selected Node: ", event.node.data);
        // data.instrumentId
        this.instrumentIdSelected = event.node.data.instrument.instrumentId;
    }
  }

  bool?: boolean;
  reloadPortfolio(reload: String) {
    if(reload == "reload"){
      this.loadPortfolio();
    }
  }
  

}


// To have buy and sell in portfolio, we need to create a new prices array for portfolio and
// populate it by finding the instrument id of that price 