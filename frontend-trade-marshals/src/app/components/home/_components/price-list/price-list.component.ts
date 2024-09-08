import { Component, OnInit } from '@angular/core';
import { Price } from 'src/app/models/price';
import { PriceService } from 'src/app/services/price.service';
import { ColDef, SideBarDef } from 'ag-grid-community';
import { SellComponent } from '../sell/sell.component';
import { BuyComponent } from '../buy/buy.component';

@Component({
  selector: 'app-price-list',
  templateUrl: './price-list.component.html',
  styleUrls: ['./price-list.component.css']
})
export class PriceListComponent implements OnInit{
  prices: Price[] = [];

  public columnDefs: ColDef[] = [{ 
    headerName: "Instrument Description", 
    field: "instrument.instrumentDescription",
    minWidth: 300,
  },{ 
    headerName: "Category ID", 
    field: "instrument.categoryId",
  },{ 
    headerName: "Ask Price", 
    field: "askPrice",
  },{ 
    headerName: "Bid Price", 
    field: "bidPrice",
  },{ 
    headerName: "Max Quantity", 
    field: "instrument.maxQuantity",
  },{ 
    headerName: "Min Quantity", 
    field: "instrument.minQuantity",
  },{ 
    headerName: "Buy", 
    field: "buy",
    cellRenderer: BuyComponent,
    cellRendererParams: {
      
    }
  },{ 
    headerName: "Sell", 
    field: "sell",
    cellRenderer: SellComponent,
    cellRendererParams: {
      
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
  ) {}

  ngOnInit(): void {
    this.loadAllPrices();
  }

  loadAllPrices() {
    this.priceService.getPrices()
      .subscribe(data => this.prices = data);
  }

  
}