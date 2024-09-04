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
});
