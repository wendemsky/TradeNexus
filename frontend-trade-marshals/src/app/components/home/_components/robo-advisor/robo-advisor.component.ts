import { Component } from '@angular/core';
import { Price } from 'src/app/models/price';
import { PriceService } from 'src/app/services/price.service';
import { ColDef, SideBarDef } from 'ag-grid-community';
import { SellComponent } from '../sell/sell.component';
import { BuyComponent } from '../buy/buy.component';

@Component({
  selector: 'app-robo-advisor',
  templateUrl: './robo-advisor.component.html',
  styleUrls: ['./robo-advisor.component.css']
})
export class RoboAdvisorComponent {

  isBuy: boolean = false;

  prices: Price[] = [];

  public buyColumnDefs: ColDef[] = [
    {
      headerName: "Instrument ID",
      field: "instrument.instrumentId",
    },
    {
      headerName: "Instrument Description",
      field: "instrument.instrumentDescription",
    },
    {
      headerName: "Category ID",
      field: "instrument.categoryId",
    },
    {
      headerName: "Bid Price",
      field: "bidPrice",
    }, 
    {
      headerName: "Buy",
      field: "buy",
      minWidth: 150,
      cellRenderer: BuyComponent,
    },

  ]

  public sellColumnDefs: ColDef[] = [
    {
      headerName: "Instrument ID",
      field: "instrument.instrumentId",
    },
    {
      headerName: "Instrument Description",
      field: "instrument.instrumentDescription",
    },
    {
      headerName: "Category ID",
      field: "instrument.categoryId",
    },
    {
      headerName: "Ask Price",
      field: "askPrice",
    }, 
    {
      headerName: "Sell",
      field: "sell",
      minWidth: 150,
      cellRenderer: SellComponent
    },
  ]

  public columnDefs: ColDef[] = [{
    headerName: "Ask Price",
    field: "askPrice",
  }, {
    headerName: "Bid Price",
    field: "bidPrice",
  }, {
    headerName: "Price Timestamp",
    field: "priceTimestamp",
  }, {
    headerName: "Instrument ID",
    field: "instrument.instrumentId",
  }, {
    headerName: "External ID Type",
    field: "instrument.externalIdType",
  }, {
    headerName: "External ID",
    field: "instrument.externalId",
  }, {
    headerName: "Category ID",
    field: "instrument.categoryId",
  }, {
    headerName: "Instrument Description",
    field: "instrument.instrumentDescription",
  }, {
    headerName: "Max Quantity",
    field: "instrument.maxQuantity",
  }, {
    headerName: "Min Quantity",
    field: "instrument.minQuantity",
  }, {
    headerName: "Min Quantity",
    field: "instrument.minQuantity",
  },
  {
    headerName: "Buy",
    field: "buy",
    cellRenderer: BuyComponent
  }, {
    headerName: "Sell",
    field: "sell",
    cellRenderer: SellComponent
  },
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
      }, {
        id: 'filters',
        labelDefault: 'Filters',
        labelKey: 'filters',
        iconKey: 'filter',
        toolPanel: 'agFiltersToolPanel',
      }],
    position: 'left',
  }

  constructor(
    private priceService: PriceService,
  ) { }

  ngOnInit(): void {
    this.loadAllPrices();
  }

  loadAllPrices() {
    this.priceService.getPrices()
      .subscribe(
        (data) => {
          this.prices = data;
          this.prices = this.prices.sort( () => 0.5 - Math.random())
          this.prices = this.prices.slice(0, 5);
        }
      );
  }
}
