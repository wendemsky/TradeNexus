import { Component, inject, OnInit } from '@angular/core';
import { CellClickedEvent, ColDef, SideBarDef } from 'ag-grid-community';
import { BuyComponent } from '../buy/buy.component';
import { SellComponent } from '../sell/sell.component';
import { ClientPortfolioService } from 'src/app/services/Client/client-portfolio.service';
import { ClientPortfolio } from 'src/app/models/Client/ClientPortfolio';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { Instrument } from 'src/app/models/Trade/instrument';
import { Holding } from 'src/app/models/Trade/Holding';
import { Price } from 'src/app/models/Trade/price';
import { PriceService } from 'src/app/services/Trade/price.service';


@Component({
  selector: 'app-portfolio',
  templateUrl: './portfolio.component.html',
  styleUrls: ['./portfolio.component.css']
})
export class PortfolioComponent implements OnInit {

  clientId: string | undefined;

  clientProfileData!: ClientProfile | null;

  portfolioData?: ClientPortfolio;
  transformedData: any[] = [];
  private _snackBar = inject(MatSnackBar);

  instrumentIdSelected?: string;

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
    private clientProfileService: ClientProfileService,
    private priceService: PriceService
  ) {

  }

  ngOnInit(): void {
    this.clientProfileService.getClientProfile().subscribe(profile => {
      this.clientProfileData = profile;
      console.log('client ID: ', this.clientProfileData?.client?.clientId);
      this.clientProfileData?.client?.clientId !== undefined ? this.clientId = this.clientProfileData?.client?.clientId : console.error('Client ID is of type undefined');
      this.loadPortfolio();
    })

  }

  loadPortfolio() {
    const snackBarConfig = new MatSnackBarConfig();
    snackBarConfig.duration = 2000;
    snackBarConfig.panelClass = ['form-submit-snackbar'];

    this.clientId ? this.clientPortfolioService.getClientPortfolio(this.clientId)
      .subscribe({
        next: (data) => {
          console.log('Portfolio Data from service: ', data);
          this.transformData(data);
          this.portfolioData = data;
          console.log('Portfolio Data after transformation: ', this.portfolioData);
        },
        error: (e) => {
          console.log('Error in loading Trade History: ', e);
          this._snackBar.open(e, '', snackBarConfig)
        }
      }) : console.error('Client ID is undefined');
  }

  // Function to flatten the holdings array into individual rows
  transformData(portfolioData: ClientPortfolio) {
    //Get prices from storage
    let prices: Price[] = []
    this.priceService.getLivePrices()
      .subscribe(data => prices = data);
    this.transformedData = portfolioData.holdings.map((holding: Holding) => {
      //Get the price data and set it in holding first
      let price = prices.find(price => price.instrument.instrumentId === holding.instrumentId); 
      if(price) {
        holding.categoryId = price?.instrument.categoryId
        holding.instrumentDesc = price?.instrument.instrumentDescription
      }
      return {
        clientId: portfolioData.clientId,
        instrumentId: holding.instrumentId,
        categoryId: holding.categoryId ? holding.categoryId : undefined,
        instrumentDesc: holding.instrumentDesc ? holding.instrumentDesc:undefined,
        quantity: holding.quantity,
        avgPrice: holding.avgPrice
      }
    });
  }

  cellClicked(event: CellClickedEvent) {
    if (event.node.data) {
      console.log("Selected Node: ", event.node.data);
      // data.instrumentId
      this.instrumentIdSelected = event.node.data.instrument.instrumentId;
    }
  }

  bool?: boolean;
  reloadPortfolio(reload: String) {
    if (reload == "reload") {
      this.loadPortfolio();
    }
  }


}
