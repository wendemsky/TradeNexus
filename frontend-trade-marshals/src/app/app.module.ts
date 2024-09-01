import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
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
import { HttpClientModule } from '@angular/common/http';

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
    RegistrationComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    MaterialModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
