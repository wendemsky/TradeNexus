import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { MaterialModule } from './material.module';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { HomeComponent } from './components/home/home.component';
import { PortfolioComponent } from './components/home/_components/portfolio/portfolio.component';
import { ReportActivityComponent } from './components/home/_components/report-activity/report-activity.component';
import { TradingHistoryComponent } from './components/home/_components/trading-history/trading-history.component';
import { ProfileComponent } from './components/home/_components/profile/profile.component';
import { ClientPreferencesComponent } from './components/home/_components/client-preferences/client-preferences.component';
import { TradingFormComponent } from './components/home/_components/trading-form/trading-form.component';
import { RegistrationComponent } from './components/registration/registration.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatSliderModule} from '@angular/material/slider';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import { AgGridModule } from 'ag-grid-angular';
import { ModuleRegistry } from '@ag-grid-community/core';
import { SideBarModule } from '@ag-grid-enterprise/side-bar';
import { PriceListComponent } from './components/home/_components/price-list/price-list.component';
import { HttpClientModule } from '@angular/common/http';
import { MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { BuyComponent } from './components/home/_components/buy/buy.component';
import { SellComponent } from './components/home/_components/sell/sell.component';

ModuleRegistry.registerModules([SideBarModule]);


@NgModule({
  declarations: [
    AppComponent,
    LandingPageComponent,
    HomeComponent,
    PortfolioComponent,
    ReportActivityComponent,
    TradingHistoryComponent,
    ProfileComponent,
    ClientPreferencesComponent,
    TradingFormComponent,
    RegistrationComponent,
    PriceListComponent,
    TradingHistoryComponent,
    BuyComponent,
    SellComponent
  ],
  imports: [
    BrowserModule,
    MatSelectModule,
    MatFormFieldModule,
    AppRoutingModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    MaterialModule,
    HttpClientModule,
    AgGridModule,
    MatSliderModule,
    MatButtonModule,
    MatCheckboxModule,
    AgGridModule,
    HttpClientModule,
    MatDialogModule,
    FormsModule,
    MatInputModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
