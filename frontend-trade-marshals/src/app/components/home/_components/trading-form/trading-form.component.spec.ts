import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TradingFormComponent } from './trading-form.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TradeService } from 'src/app/services/trade.service';
import { TradeHistoryService } from 'src/app/services/trade-history.service';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MaterialModule } from 'src/app/material.module';
import { Component } from 'ag-grid-community';
import { AgGridAngular } from 'ag-grid-angular';
import { ViewChild } from '@angular/core';

// @Component({
//   selector: 'app-grid-angular',
//   template: '',
// })
// export class TestGridComponent {
//   @ViewChild(AgGridAngular) public agGrid?: AgGridAngular;
// }

describe('TradingFormComponent', () => {
  let component: TradingFormComponent;
  let fixture: ComponentFixture<TradingFormComponent>;

  let tradeMockService: any = jasmine.createSpyObj('TradeService', ['getClientProfile']);

  let tradeHistoryMockService: any = jasmine.createSpyObj('TradeHistoryService', ['getClientProfile']);
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        TradingFormComponent,
      ],
      imports: [
        HttpClientTestingModule,
        MatDialogModule
      ],
      providers: [
        {provide: TradeService, useValue: tradeMockService},
        {provide: TradeHistoryService, useValue: tradeHistoryMockService},
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(TradingFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
