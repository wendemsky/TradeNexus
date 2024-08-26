import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientPreferencesComponent } from './client-preferences.component';

describe('ClientPreferencesComponent', () => {
  let component: ClientPreferencesComponent;
  let fixture: ComponentFixture<ClientPreferencesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClientPreferencesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClientPreferencesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
