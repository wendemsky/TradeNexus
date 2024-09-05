import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportActivityComponent } from './report-activity.component';
import { MaterialModule } from 'src/app/material.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('ReportActivityComponent', () => {
  let component: ReportActivityComponent;
  let fixture: ComponentFixture<ReportActivityComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReportActivityComponent ],
      imports: [
        MaterialModule,
        ReactiveFormsModule
      ],
      providers: [
        provideAnimations() 
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReportActivityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should log the report value to the console on submit', () => {
    // Spy on console.log
    spyOn(console, 'log');

    // Call the onSubmit method
    component.onSubmit();

    // Check that console.log was called with the correct arguments
    expect(console.log).toHaveBeenCalledWith('Report type -> ' + JSON.stringify(component.report.value));
  });
  
});
