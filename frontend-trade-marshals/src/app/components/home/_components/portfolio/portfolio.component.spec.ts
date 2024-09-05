import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PortfolioComponent } from './portfolio.component';
import { ClientPortfolioService } from 'src/app/services/Client/client-portfolio.service';
import { of } from 'rxjs';
import { AgGridModule } from 'ag-grid-angular';

const testPortfolioData = {
  "id": "aabd",
  "clientId": "1654658069",
  "currBalance": 10000,
  "holdings": []
}

describe('PortfolioComponent', () => {
  let component: PortfolioComponent;
  let fixture: ComponentFixture<PortfolioComponent>;

  let clientPortfolioMockService: any = jasmine.createSpyObj('ClientPortfolioService', ['getClientPortfolio']);
  let getPortfolioSpy = clientPortfolioMockService.getClientPortfolio.and.returnValue(of(testPortfolioData));

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PortfolioComponent],
      imports: [
        AgGridModule
      ],
      providers: [
        {provide: ClientPortfolioService, useValue: clientPortfolioMockService}
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(PortfolioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
