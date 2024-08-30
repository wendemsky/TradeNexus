import { NgModule } from '@angular/core';
import {MatToolbarModule} from "@angular/material/toolbar"
import {MatDividerModule} from '@angular/material/divider';
import {MatMenuModule} from "@angular/material/menu"
import {MatIconModule} from "@angular/material/icon" 
import {MatTooltipModule} from "@angular/material/tooltip"
import {MatDialogModule} from '@angular/material/dialog';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatCardModule} from "@angular/material/card"

import {MatSliderModule} from '@angular/material/slider';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';


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
        MatSliderModule,
        MatButtonModule,
        MatCheckboxModule,
        MatFormFieldModule,
        MatSelectModule
  ],
 
})
export class MaterialModule { }