import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { ClientPreferencesComponent } from './components/home/_components/client-preferences/client-preferences.component';
import { RegistrationComponent } from './components/registration/registration.component';

const routes: Routes = [
  {path: 'register', component: RegistrationComponent},
  {path: 'client-preferences', component: ClientPreferencesComponent},
  {path: '', component: LandingPageComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
