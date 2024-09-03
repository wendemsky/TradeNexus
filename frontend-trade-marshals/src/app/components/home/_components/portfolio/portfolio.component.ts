import { Component } from '@angular/core';
import { ColDef, SideBarDef } from 'ag-grid-community';
import { BuyComponent } from '../buy/buy.component';
import { SellComponent } from '../sell/sell.component';


@Component({
  selector: 'app-portfolio',
  templateUrl: './portfolio.component.html',
  styleUrls: ['./portfolio.component.css']
})
export class PortfolioComponent {
  public columnDefs: ColDef[] = [
    { 
      headerName: "Instrument Description", 
      field: "instrumentDescription",
      minWidth: 400,
    },
    { 
      headerName: "Category", 
      field: "categoryId",
    },
    { 
      headerName: "Quantity", 
      field: "quantity",
    }, 
    { 
      headerName: "Current Price", 
      field: "currentPrice",
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

}
