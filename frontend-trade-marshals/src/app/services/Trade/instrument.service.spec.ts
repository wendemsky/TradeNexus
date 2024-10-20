import { fakeAsync, inject, TestBed, tick } from '@angular/core/testing';

import { InstrumentService } from './instrument.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { MaterialModule } from '../../material.module';
import { HttpErrorResponse } from '@angular/common/http';

describe('InstrumentService', () => {
  let service: InstrumentService;
  let httpTestingController: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        MaterialModule
      ]
    });
    service = TestBed.inject(InstrumentService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should handle a 404 error', inject([InstrumentService],
    fakeAsync((service: InstrumentService) => {
    let errorResp: HttpErrorResponse;
    let errorReply: string = '';
    const errorHandlerSpy = spyOn(service,'handleError')
    .and.callThrough();
    service.getInstruments()
    .subscribe({next: () => fail('Should not succeed'),
    error: (err) => errorReply = err});
    const req = httpTestingController.expectOne('http://localhost:3000/fmts/trades/instruments');
    // Assert that the request is a GET.
    expect(req.request.method).toEqual('GET');
    // Respond with error
    req.flush('Forced 404', {
      status: 404,
      statusText: 'Not Found'
    });
    // Cause all Observables to complete and check the results
    tick();
    expect(errorReply).toBe('Unexpected error at service while trying to fetch instruments. Please try again later!');
    expect(errorHandlerSpy).toHaveBeenCalled();
    errorResp = errorHandlerSpy.calls.argsFor(0)[0];
    expect(errorResp.status).toBe(404);
  })));

  it('should handle a network error', inject(
    [InstrumentService],
    fakeAsync((service: InstrumentService) => {
      let errorResp: HttpErrorResponse;
      let errorReply: string = '';
      const errorHandlerSpy = spyOn(service, 'handleError').and.callThrough();
      service.getInstruments().subscribe({
        next: () => fail('Should fail'),
        error: (err) => (errorReply = err),
      });
      const req = httpTestingController.expectOne('http://localhost:3000/fmts/trades/instruments');
      expect(req.request.method).toEqual('GET');
      const error = new ProgressEvent('Network Error');
      req.error(error);
      httpTestingController.verify();
      tick();
      expect(errorReply).toBe(
        'Unexpected error at service while trying to fetch instruments. Please try again later!'
      );
      expect(errorHandlerSpy).toHaveBeenCalled();
      errorResp = errorHandlerSpy.calls.argsFor(0)[0];
    })
  ));
});
