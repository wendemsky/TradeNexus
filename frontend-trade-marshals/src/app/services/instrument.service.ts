import { Injectable } from '@angular/core';
import { Instrument } from '../models/instrument';
import { Observable, of } from 'rxjs';
import { MockInstruments } from '../../assets/mock-data/mock-instruments';

@Injectable({
  providedIn: 'root'
})
export class InstrumentService {
  instruments: Instrument[] = MockInstruments;

  constructor() { }

  getInstruments(): Observable<Instrument[]> {
    return of(this.instruments);
  }
}
