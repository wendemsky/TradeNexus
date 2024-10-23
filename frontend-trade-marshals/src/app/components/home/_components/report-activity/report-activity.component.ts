import { Component, inject, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';
import { ColDef, SideBarDef } from 'ag-grid-community';
import { ClientPortfolio } from 'src/app/models/Client/ClientPortfolio';
import { Holding } from 'src/app/models/Trade/Holding';
import { Price } from 'src/app/models/Trade/price';
import { ClientPortfolioService } from 'src/app/services/Client/client-portfolio.service';
import { PriceService } from 'src/app/services/Trade/price.service';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { TradeHistoryService } from 'src/app/services/Trade/trade-history.service';
import { Trade } from 'src/app/models/Trade/trade';
import { ClientActivityReportService } from 'src/app/services/Client/client-activity-report.service';


@Component({
  selector: 'app-report-activity',
  templateUrl: './report-activity.component.html',
  styleUrls: ['./report-activity.component.css']
})
export class ReportActivityComponent implements OnInit {

  clientId: string | undefined;

  clientProfileData!: ClientProfile | null;

  portfolioData?: ClientPortfolio;
  transformedData: any[] = [];
  public tradeHistoryData: Trade[] = [];
  private _snackBar = inject(MatSnackBar);

  instrumentIdSelected?: string;

  public holdingsColumnDefs: ColDef[] = [
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
    }
  ]

  public tradesColumnDefs: ColDef[] = [{
    headerName: "Instrument ID",
    field: "instrumentId"
  }, {
    headerName: "Quantity",
    field: "quantity",
  }, {
    headerName: "Execution Price",
    field: "executionPrice",
  }, {
    headerName: "Direction",
    field: "direction",
  }, {
    headerName: "Cash Value",
    field: "cashValue",
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

  report_types = [
    { value: "Holdings Report", name: "Holdings Report" }, //Get clients holdings
    // {value:"P&L Report",name: "P&L Report"}, //Get Clients P&L
    { value: "Trade Report", name: "Trade Report" }, //Get Clients trading history
  ]

  //For now hardcoding clients - But should retrieve from service
  clients = [
    { value: '1654658069', name: 'Sowmya' },
    { value: '541107416', name: 'Himanshu' },
    { value: '1425922638', name: 'Rishiyanth' },
    { value: '1644724236', name: 'Aditi' },
    { value: '1236679496', name: 'Mohith' }
  ]

  constructor(
    private clientProfileService: ClientProfileService,
    private clientPortfolioService: ClientPortfolioService,
    private priceService: PriceService,
    private tradeHistoryService: TradeHistoryService,
    private clientActivityReportService: ClientActivityReportService
  ) { }

  ngOnInit() {
    this.clientProfileService.getClientProfile().subscribe(profile => {
      this.clientProfileData = profile;
      console.log('client ID: ', this.clientProfileData?.client?.clientId);
      this.clientProfileData?.client?.clientId !== undefined ? this.clientId = this.clientProfileData?.client?.clientId : console.error('Client ID is of type undefined');
      console.log(this.clientId);
    })
  }

  //Form Groups for logged in client's report
  yourReport: FormGroup = new FormGroup({
    reportType: new FormControl('', Validators.required)
  })

  //Form Groups for other client's report
  othersReport: FormGroup = new FormGroup({
    clientId: new FormControl('', Validators.required),
    reportType: new FormControl('', Validators.required)
  })

  //On Submitting logged in clients report
  onSubmitYourReportForm() {
    console.log("Your Report type -> " + JSON.stringify(this.yourReport.value));
    console.log(this.clientId);
    if (this.yourReport.value.reportType == "Holdings Report") {
      // this.clientId ? this.clientPortfolioService.getClientPortfolio(this.clientId)
      this.clientId ? this.clientActivityReportService.getClientHoldingsReport(this.clientId)
        .subscribe({
          next: (data) => {
            console.log('Holdings Data from service: ', data);
            this.transformData(data);
            this.portfolioData = data;
            console.log('Holdings Data after transformation: ', this.portfolioData);
          },
          error: (e) => {
            console.log('Failed to load client portfolio data: ', e);
          }
        }) : console.error('Client ID is undefined');
    }

    if (this.yourReport.value.reportType == "Trade Report") {
      // this.clientId !== undefined ? this.tradeHistoryService.getTrades(this.clientId)
      this.clientId !== undefined ? this.clientActivityReportService.getClientTradeReport(this.clientId)
        .subscribe({
          next: (data: any) => {
            this.tradeHistoryData = data.trades;
            this.tradeHistoryData = this.tradeHistoryData.reverse()
          },
          error: (e) => {
            console.log('Error in loading Trade History: ', e);
            this._snackBar.open(e, '', {
              duration: 3000,
            })
          }
        }) : console.error('Client ID is undefined.')
    }
    // P&L report

    if (this.yourReport.value.reportType == "P&L Report") {
      // this.clientId ? this.clientPortfolioService.getClientPortfolio(this.clientId)
      this.clientId ? this.clientActivityReportService.getClientProfitLossReport(this.clientId)
        .subscribe({
          next: (data) => {
            console.log('P&L Data from service: ', data);
            this.transformData(data);
            this.portfolioData = data;
            console.log('P&L Data after transformation: ', this.portfolioData);
          },
          error: (e) => {
            console.log('Failed to load client portfolio data: ', e);
          }
        }) : console.error('Client ID is undefined');
    }

  }

  //On Submitting other clients report
  onSubmitOthersReportForm() {
    console.log(`Report type of Client ${this.othersReport.value.clientId} is ${this.othersReport.value.reportType}`);
    if (this.othersReport.value.reportType == "Holdings Report") {
      // this.othersReport.value.clientId ? this.clientPortfolioService.getClientPortfolio(this.othersReport.value.clientId)
      console.log(this.othersReport.value.clientId);
      this.othersReport.value.clientId ? this.clientActivityReportService.getClientHoldingsReport(this.othersReport.value.clientId)
        .subscribe({
          next: (data) => {
            console.log('Portfolio Data from service: ', data);
            this.transformData(data);
            this.portfolioData = data;
            console.log('Portfolio Data after transformation: ', this.portfolioData);
          },
          error: (e) => {
            console.log('Failed to load client portfolio data: ', e);
          }
        }) : console.error('Client ID is undefined');
    }

    if (this.othersReport.value.reportType == "Trade Report") {
      // this.othersReport.value.clientId !== undefined ? this.tradeHistoryService.getTrades(this.othersReport.value.clientId)
      console.log(this.othersReport.value.clientId);
      this.othersReport.value.clientId !== undefined ? this.clientActivityReportService.getClientTradeReport(this.othersReport.value.clientId)
        .subscribe({
          next: (data: any) => {
            this.tradeHistoryData = data.trades;
            this.tradeHistoryData = this.tradeHistoryData.reverse()
          },
          error: (e) => {
            console.log('Error in loading Trade History: ', e);
            this._snackBar.open(e, '', {
              duration: 3000,
            })
          }
        }) : console.error('Client ID is undefined.')
    }
  }

  transformData(portfolioData: any) {
    //Get prices from storage
    let prices: Price[] = []
    this.priceService.getLivePrices()
      .subscribe(data => prices = data);
    this.transformedData = portfolioData.map((holding: Holding) => {
      //Get the price data and set it in holding first
      let price = prices.find(price => price.instrument.instrumentId === holding.instrumentId);
      if (price) {
        holding.categoryId = price?.instrument.categoryId
        holding.instrumentDesc = price?.instrument.instrumentDescription
      }
      return {
        clientId: portfolioData.clientId,
        instrumentId: holding.instrumentId,
        categoryId: holding.categoryId ? holding.categoryId : undefined,
        instrumentDesc: holding.instrumentDesc ? holding.instrumentDesc : undefined,
        quantity: holding.quantity,
        avgPrice: holding.avgPrice
      }
    });
  }
}