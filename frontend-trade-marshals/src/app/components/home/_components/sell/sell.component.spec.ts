import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SellComponent } from './sell.component';
import { PriceService } from 'src/app/services/price.service';
import { MatDialogModule } from '@angular/material/dialog';
import { of } from 'rxjs';

const mockPrices = [
  {
    "askPrice": 104.75,
    "bidPrice": 104.25,
    "priceTimestamp": "21-AUG-19 10.00.01.042000000 AM GMT",
    "instrument": {
      "instrumentId": "N123456",
      "externalIdType": "CUSIP",
      "externalId": "46625H100",
      "categoryId": "STOCK",
      "instrumentDescription": "JPMorgan Chase & Co. Capital Stock",
      "maxQuantity": 1000,
      "minQuantity": 1
    }
  },
  {
    "askPrice": 312500,
    "bidPrice": 312000,
    "priceTimestamp": "21-AUG-19 05.00.00.040000000 AM -05:00",
    "instrument": {
      "instrumentId": "N123789",
      "externalIdType": "ISIN",
      "externalId": "US0846707026",
      "categoryId": "STOCK",
      "instrumentDescription": "Berkshire Hathaway Inc. Class A",
      "maxQuantity": 10,
      "minQuantity": 1
    }
  }
]

describe('SellComponent', () => {
  let component: SellComponent;
  let fixture: ComponentFixture<SellComponent>;

  let priceMockService = jasmine.createSpyObj('PriceService', ['getPrices'])
  let getSpy = priceMockService.getPrices.and.returnValue(of(mockPrices))

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SellComponent ],
      imports: [
        MatDialogModule
      ],
      providers: [
        {provide: PriceService, useValue: priceMockService}
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SellComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
