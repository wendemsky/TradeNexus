import { fakeAsync, inject, TestBed, tick } from '@angular/core/testing';

import { RegisterService } from './register.service';
import { Client } from 'src/app/models/Client/Client';
import { ClientPortfolio } from 'src/app/models/Client/ClientPortfolio';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';

describe('RegisterService', () => {
  let service: RegisterService;
  let httpTestingController: HttpTestingController

  beforeEach(() => {

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(RegisterService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return a client if the government ID matches for validation', () => {
    let testClients:Client[] = [
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
    const govtID = { type: 'Aadhar', value: '543623546238' };
    service.checkUniqueGovtIDDetails(govtID).subscribe(client => {
      expect(client).toEqual(testClients[1]); //Must match this client
    });

    const req = httpTestingController.expectOne(service.clientDataURL);
    expect(req.request.method).toBe('GET');
    req.flush(testClients);
  });

  it('should save a new client with a POST restful service request', inject([RegisterService],
    fakeAsync((service: RegisterService) => {
      const expectedClient: Client = {
        "email": "client.new@gmail.com",
        "clientId": "1234567890",
        "password": "Password123",
        "name": "New Client",
        "dateOfBirth": "01/02/2002",
        "country": "India",
        "identification": [
          {
            "type": "Aadhar",
            "value": "543623633245"
          }
        ],
        "isAdmin": false
      }

      service.saveClientDetails(expectedClient).subscribe();
      const req = httpTestingController.expectOne(service.clientDataURL);    //Checking if the url is right
      expect(req.request.method).toEqual('POST');      // Assert that the request is a POST
      expect(req.request.body).toBe(expectedClient); //Expect that the posted client is same as the sent one
    })));

  it("should save new client's portfolio with a POST restful service request", inject([RegisterService],
    fakeAsync((service: RegisterService) => {
      const expectedClientPortfolio: ClientPortfolio = {
        "clientId": "1234567890",
        "currBalance": 10000,
        "holdings": []
      }

      service.saveClientPortfolioDetails(expectedClientPortfolio).subscribe();
      const req = httpTestingController.expectOne(service.clientPortfolioDataURL);    //Checking if the url is right
      expect(req.request.method).toEqual('POST');      // Assert that the request is a POST
      expect(req.request.body).toBe(expectedClientPortfolio); //Expect that the posted client's portfolio is same as the sent one
    })));

  /*TESTING FOR FAILURES*/
  //Test to check if our service handles a 404 error - TESTING FOR FAILURES
  it('should handle a 404 error', inject([RegisterService],
    fakeAsync((service: RegisterService) => {
      const expectedClient: Client = {
        "email": "client.new@gmail.com",
        "clientId": "1234567890",
        "password": "Password123",
        "name": "New Client",
        "dateOfBirth": "01/02/2002",
        "country": "India",
        "identification": [
          {
            "type": "Aadhar",
            "value": "543623633245"
          }
        ],
        "isAdmin": false
      }
      let errorResp: HttpErrorResponse;
      let errorReply: string = '';
      const errorHandlerSpy = spyOn(service, 'handleError')
        .and.callThrough();
      service.saveClientDetails(expectedClient) //Trying to save a new client
        .subscribe({
          next: () => fail('Should fail'),
          error: (err) => errorReply = err
        });
      const req = httpTestingController.expectOne(service.clientDataURL); //Checking if url is right
      expect(req.request.method).toEqual('POST'); //Checking if method is right
      //Flushing with error
      req.flush('Forced 404', {
        status: 404,
        statusText: 'Not Found'
      });
      httpTestingController.verify()
      tick();
      expect(errorReply).toBe('Unexpected error at service while trying to register user. Please try again later!');
      expect(errorHandlerSpy).toHaveBeenCalled();
      errorResp = errorHandlerSpy.calls.argsFor(0)[0];
      expect(errorResp.status).toBe(404);
    })));

  //Test to check if our service handles network error - TESTING FOR FAILURES
  it('should handle a network error', inject([RegisterService],
    fakeAsync((service: RegisterService) => {
      const expectedClientPortfolio: ClientPortfolio = {
        "clientId": "1234567890",
        "currBalance": 10000,
        "holdings": []
      }
      let errorResp: HttpErrorResponse;
      let errorReply: string = '';
      const errorHandlerSpy = spyOn(service, 'handleError')
        .and.callThrough();
      service.saveClientPortfolioDetails(expectedClientPortfolio) //Trying to save a new client'S Portfolio
        .subscribe({
          next: () => fail('Should fail'),
          error: (err) => errorReply = err
        });
      const req = httpTestingController.expectOne(service.clientPortfolioDataURL); //Checking if url is right
      expect(req.request.method).toEqual('POST'); //Checking if method is right
      //Flushing with error
      const error = new ProgressEvent('Network Error')
      req.error(error)
      httpTestingController.verify()
      tick();
      expect(errorReply).toBe('Unexpected error at service while trying to register user. Please try again later!');
      expect(errorHandlerSpy).toHaveBeenCalled();
    })));
});