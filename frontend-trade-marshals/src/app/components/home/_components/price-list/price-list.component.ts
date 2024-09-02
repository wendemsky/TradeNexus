import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Instrument } from 'src/app/models/instrument';
import { Price } from 'src/app/models/price';
import { PriceService } from 'src/app/services/price.service';
import { TradingFormComponent } from '../trading-form/trading-form.component';
import { ColDef, SideBarDef } from 'ag-grid-community';

@Component({
  selector: 'app-price-list',
  templateUrl: './price-list.component.html',
  styleUrls: ['./price-list.component.css']
})
export class PriceListComponent implements OnInit{
  prices: Price[] = [];

  public columnDefs: ColDef[] = [{ 
    headerName: "Ask Price", 
    field: "askPrice",
  },{ 
    headerName: "Bid Price", 
    field: "bidPrice",
  },{ 
    headerName: "Price Timestamp", 
    field: "priceTimestamp",
  },{ 
    headerName: "Instrument ID", 
    field: "instrument.instrumentId",
  },{ 
    headerName: "External ID Type", 
    field: "instrument.externalIdType",
  },{ 
    headerName: "External ID", 
    field: "instrument.externalId",
  },{ 
    headerName: "Category ID", 
    field: "instrument.categoryId",
  },{ 
    headerName: "Instrument Description", 
    field: "instrument.instrumentDescription",
  },{ 
    headerName: "Max Quantity", 
    field: "instrument.maxQuantity",
  },{ 
    headerName: "Min Quantity", 
    field: "instrument.minQuantity",
  }]

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
    },{
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
    private _dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadAllPrices();
  }

  loadAllPrices() {
    this.priceService.getPrices()
      .subscribe(data => this.prices = data);
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
      "width": '600px',
      "maxHeight": '90vh',
      data: tradeFormData
    });
  }
}
