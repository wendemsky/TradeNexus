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

  buyStocks: any[] = [
    {
      "askPrice": 0.998125,
      "bidPrice": 0.99828125,
      "priceTimestamp": "21-AUG-19 10.00.02.002000000 AM GMT",
      "instrument": {
      "instrumentId": "T67894",
      "externalIdType": "CUSIP",
      "externalId": "9128285Z9",
      "categoryId": "GOVT",
      "instrumentDescription": "USA, Note 2.5 31jan2024 5Y",
      "maxQuantity": 10000,
      "minQuantity": 100
      }
      },
      {
      "askPrice": 1,
      "bidPrice": 1.00015625,
      "priceTimestamp": "21-AUG-19 10.00.02.002000000 AM GMT",
      "instrument": {
      "instrumentId": "T67895",
      "externalIdType": "CUSIP",
      "externalId": "9128286A3",
      "categoryId": "GOVT",
      "instrumentDescription": "USA, Note 2.625 31jan2026 7Y",
      "maxQuantity": 10000,
      "minQuantity": 100
      }
      },
      {
      "askPrice": 0.999375,
      "bidPrice": 0.999375,
      "priceTimestamp": "21-AUG-19 10.00.02.002000000 AM GMT",
      "instrument": {
      "instrumentId": "T67897",
      "externalIdType": "CUSIP",
      "externalId": "9128285X4",
      "categoryId": "GOVT",
      "instrumentDescription": "USA, Note 2.5 31jan2021 2Y",
      "maxQuantity": 10000,
      "minQuantity": 100
      }
      },
      {
      "askPrice": 0.999375,
      "bidPrice": 0.999375,
      "priceTimestamp": "21-AUG-19 10.00.02.002000000 AM GMT",
      "instrument": {
      "instrumentId": "T67899",
      "externalIdType": "CUSIP",
      "externalId": "9128285V8",
      "categoryId": "GOVT",
      "instrumentDescription": "USA, Notes 2.5% 15jan2022 3Y",
      "maxQuantity": 10000,
      "minQuantity": 100
      }
      },
  ]

  longText = `The Shiba Inu is the smallest of the six original and distinct spitz breeds of dog
  from Japan. A small, agile dog that copes very well with mountainous terrain, the Shiba Inu was
  originally bred for hunting.`;

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
