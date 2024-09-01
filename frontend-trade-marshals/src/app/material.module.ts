import { NgModule } from '@angular/core';
import {MatToolbarModule} from "@angular/material/toolbar"
import {MatDividerModule} from '@angular/material/divider';
import {MatMenuModule} from "@angular/material/menu"
import {MatIconModule} from "@angular/material/icon" 
import {MatTooltipModule} from "@angular/material/tooltip"
import {MatDialogModule} from '@angular/material/dialog';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatCardModule} from "@angular/material/card"

import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input'
import {MatSliderModule} from '@angular/material/slider';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatSelectModule} from '@angular/material/select';
import { MatStepperModule } from '@angular/material/stepper';


@NgModule({
    exports: [
        MatToolbarModule,
        MatDividerModule,
        MatMenuModule,
        MatIconModule,
        MatTooltipModule,
        MatDialogModule,
        MatSnackBarModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatSliderModule,
        MatButtonModule,
        MatCheckboxModule,
        MatFormFieldModule,
        MatSelectModule,
        MatStepperModule
  ],
 
})
export class MaterialModule { }