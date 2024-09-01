import { Component } from '@angular/core';
import { ColDef, GridOptions, SideBarDef } from 'ag-grid-community';

@Component({
  selector: 'app-trading-history',
  templateUrl: './trading-history.component.html',
  styleUrls: ['./trading-history.component.css']
})
export class TradingHistoryComponent {
  public columnDefs: ColDef[] = [
    { 
      headerName: "Instrument Description", 
      field: "instrumentDescription",
      minWidth: 400
    },
    { 
      headerName: "Quantity", 
      field: "quantity",
    },
    { 
      headerName: "Direction", 
      field: "Direction",
    }, 
    { 
      headerName: "Cash Value of Trade", 
      field: "cashValue ",
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
