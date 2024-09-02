import { Component, OnInit } from '@angular/core';
import { ColDef, GridOptions, SideBarDef } from 'ag-grid-community';
import { Trade } from 'src/app/models/trade';
import { TradeHistoryService } from 'src/app/services/trade-history.service';

@Component({
  selector: 'app-trading-history',
  templateUrl: './trading-history.component.html',
  styleUrls: ['./trading-history.component.css']
})
export class TradingHistoryComponent  implements OnInit{

  

  public columnDefs: ColDef[] = [{ 
      headerName: "Instrument ID", 
      field: "instrumentId"
    },{ 
      headerName: "Quantity", 
      field: "quantity",
    },{ 
      headerName: "Execution Price", 
      field: "executionPrice",
    },{ 
      headerName: "Direction", 
      field: "direction",
    },{ 
      headerName: "Client ID", 
      field: "clientId",
    },{ 
      headerName: "Trade ID", 
      field: "tradeId",
    }, { 
      headerName: "Cash Value", 
      field: "cashValue ",
  }]

  public tradeHistoryData: Trade[] = [];


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

  constructor(private tradeHistoryService: TradeHistoryService) {
    
  }

  ngOnInit(): void {
    this.loadTrades();
  }

  loadTrades() {
    this.tradeHistoryService.getTrades()
      .subscribe(data => this.tradeHistoryData = data);
  }

}
