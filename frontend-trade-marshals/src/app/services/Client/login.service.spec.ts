import { fakeAsync, inject, TestBed, tick } from '@angular/core/testing';

import { LoginService } from './login.service';
import { Client } from 'src/app/models/Client/Client';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';


describe('LoginService', () => {
  let service: LoginService;
  let httpTestingController: HttpTestingController

  let testClients: Client[] = [] //Test valid Clients

  beforeEach(() => {
    testClients = [
      {
        "email": "client.1@gmail.com",
        "clientId": "5413074269",
        "password": "Marsh2024",
        "name": "Client 1",
        "dateOfBirth": "08/12/2002",
        "country": "India",
        "identification": [
          {
            "type": "Aadhar",
            "value": "123456789102"
          }
        ],
        "isAdmin": false
      },
      {
        "email": "client.2@gmail.com",
        "clientId": "5423407272",
        "password": "Marsh2024",
        "name": "Client 2",
        "dateOfBirth": "20/11/2002",
        "country": "India",
        "identification": [
          {
            "type": "Aadhar",
            "value": "543623546238"
          }
        ],
        "isAdmin": false
      }
    ]

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(LoginService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return client from restful service given email ID of a client that exists', inject([LoginService],
    fakeAsync((service: LoginService) => {
      let client!: Client | null
      const testClientEmail: string = 'client.1@gmail.com'
      service.getValidClientDetails(testClientEmail)
        .subscribe(data => client = data);
      const req = httpTestingController.expectOne(service.dataURL);    //Checking if the url is right
      expect(req.request.method).toEqual('GET');      // Assert that the request is a GET
      req.flush(testClients); // Respond with mock data, causing Observable to resolve 
      tick();
      expect(client?.email).toBe(testClientEmail); //Expect that returned client's email matches
    })));

  it('should return null from restful service given email ID of a non existent client', inject([LoginService],
    fakeAsync((service: LoginService) => {
      let client!: Client | null;
      const testClientEmail: string = 'invalid.email@gmail.com';
      service.getValidClientDetails(testClientEmail)
        .subscribe(data => client = data);

      const req = httpTestingController.expectOne(service.dataURL); // Checking if the URL is right
      expect(req.request.method).toEqual('GET'); // Assert that the request is a GET
      req.flush(testClients); // Respond with mock data, causing Observable to resolve
      tick();
      expect(client).toBeFalsy(); // Expect the client to be null for an invalid email
    })
  ));

  /*TESTING FOR FAILURES*/
  //Test to check if our service handles a 404 error - TESTING FOR FAILURES
  it('should handle a 404 error', inject([LoginService],
    fakeAsync((service: LoginService) => {
      let errorResp: HttpErrorResponse;
      let errorReply: string = '';
      const errorHandlerSpy = spyOn(service, 'handleError')
        .and.callThrough();
      service.getValidClientDetails('sample@gmail.com')
        .subscribe({
          next: () => fail('Should fail'),
          error: (err) => errorReply = err
        });
      const req = httpTestingController.expectOne(service.dataURL); //Checking if url is right
      expect(req.request.method).toEqual('GET'); //Checking if method is right
      //Flushing with error
      req.flush('Forced 404', {
        status: 404,
        statusText: 'Not Found'
      });
      httpTestingController.verify()
      tick();
      expect(errorReply).toBe('Unexpected error at service while trying to login user. Please try again later!');
      expect(errorHandlerSpy).toHaveBeenCalled();
      errorResp = errorHandlerSpy.calls.argsFor(0)[0];
      expect(errorResp.status).toBe(404);
    })));

  //Test to check if our service handles network error - TESTING FOR FAILURES
  it('should handle a network error', inject([LoginService],
    fakeAsync((service: LoginService) => {
      let errorReply: string = '';
      const errorHandlerSpy = spyOn(service, 'handleError')
        .and.callThrough();
      service.getValidClientDetails('sample@gmail.com')
        .subscribe({
          next: () => fail('Should fail'),
          error: (err) => errorReply = err
        });
      const req = httpTestingController.expectOne(service.dataURL); //Checking if url is right
      expect(req.request.method).toEqual('GET'); //Checking if method is right
      //Flushing with error
      const error = new ProgressEvent('Network Error')
      req.error(error)
      httpTestingController.verify()
      tick();
      expect(errorReply).toBe('Unexpected error at service while trying to login user. Please try again later!');
      expect(errorHandlerSpy).toHaveBeenCalled();
    })));


});
