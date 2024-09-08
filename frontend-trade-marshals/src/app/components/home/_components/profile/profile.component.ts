import { Component, OnInit } from '@angular/core';
import { ClientProfile } from 'src/app/models/Client/ClientProfile';
import { ClientProfileService } from 'src/app/services/Client/client-profile.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  clientProfileData!: ClientProfile | null //Client Profile data that is set with ClientProfileService

  constructor(private clientProfileService: ClientProfileService) { }
  ngOnInit() {
    //Getting Client Profile data from service
    this.clientProfileService.getClientProfile().subscribe(profile => {
      this.clientProfileData = profile
      console.log('Logged In Client Profile Data: ', this.clientProfileData);
    })
  }

}
