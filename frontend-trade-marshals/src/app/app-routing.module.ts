import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { RegistrationComponent } from './components/registration/registration.component';
import { HomeComponent } from './components/home/home.component';
import { ProfileComponent } from './components/home/_components/profile/profile.component';
import { ClientPreferencesComponent } from './components/home/_components/client-preferences/client-preferences.component';
import { PortfolioComponent } from './components/home/_components/portfolio/portfolio.component';
import { TradingHistoryComponent } from './components/home/_components/trading-history/trading-history.component';
import { ReportActivityComponent } from './components/home/_components/report-activity/report-activity.component';

const routes: Routes = [
  {path: '', component: LandingPageComponent},
  {path: 'register', component: RegistrationComponent},
  {path: 'home', component: HomeComponent,
    children: [
      {path: 'profile', component:ProfileComponent},
      {path: 'client-preferences', component: ClientPreferencesComponent},
      {path: 'client-portfolio', component: PortfolioComponent},
      {path: 'client-trading-history', component: TradingHistoryComponent},
      {path: 'report-activity', component: ReportActivityComponent}
    ]
  },
  {path:'**', redirectTo:'/'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
