import { Component } from '@angular/core';
import { ColDef, GridOptions, SideBarDef } from 'ag-grid-community';


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
