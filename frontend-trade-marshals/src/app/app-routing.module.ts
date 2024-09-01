import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { ClientPreferencesComponent } from './components/home/_components/client-preferences/client-preferences.component';
import { RegistrationComponent } from './components/registration/registration.component';
import { HomeComponent } from './components/home/home.component';

const routes: Routes = [
  {path: '', component: LandingPageComponent},
  {path: 'register', component: RegistrationComponent},
  {path: 'home', component: HomeComponent},
  {path: 'client-preferences', component: ClientPreferencesComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
