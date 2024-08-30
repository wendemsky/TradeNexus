import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { ClientPreferencesComponent } from './components/home/_components/client-preferences/client-preferences.component';

const routes: Routes = [
  {path: '', component: LandingPageComponent},
  {path: 'client-preferences', component: ClientPreferencesComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
