import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { HomeComponent } from './components/home/home.component';
import { PortfolioComponent } from './components/home/_components/portfolio/portfolio.component';
import { ReportActivityComponent } from './components/home/_components/report-activity/report-activity.component';
import { TradingHistoryComponent } from './components/home/_components/trading-history/trading-history.component';
import { ProfileComponent } from './components/home/_components/profile/profile.component';
import { ClientPreferencesComponent } from './components/home/_components/client-preferences/client-preferences.component';
import { BuyTradeComponent } from './components/home/_components/buy-trade/buy-trade.component';
import { TradingFormComponent } from './components/home/_components/trading-form/trading-form.component';
import { RegistrationComponent } from './components/registration/registration.component';
import { ReactiveFormsModule } from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatSliderModule} from '@angular/material/slider';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';

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
    BuyTradeComponent,
    TradingFormComponent,
    RegistrationComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    BrowserAnimationsModule,
    MatSliderModule,
    MatButtonModule,
    MatCheckboxModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
