import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ClientPreferencesComponent } from './components/home/_components/client-preferences/client-preferences.component';

const routes: Routes = [
  {path: 'client-preferences', component: ClientPreferencesComponent},

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
