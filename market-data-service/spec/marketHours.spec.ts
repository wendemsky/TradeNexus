import { DateTime } from 'luxon'
import { isNYSEOpen, secondsUntilNextOpen } from '../src/lib/marketHours.js'

describe('isNYSEOpen', () => {
  let dateSpy: jasmine.Spy

  beforeEach(() => {
    dateSpy = spyOn(DateTime, 'now')
  })

  function mockET(weekday: number, hour: number, minute: number): void {
    const fake = DateTime.fromObject(
      { year: 2025, month: 10, day: 13 + weekday - 1, hour, minute },
      { zone: 'America/New_York' }
    )
    dateSpy.and.returnValue(fake)
  }

  it('returns true during trading hours on a weekday', () => {
    mockET(1, 10, 0)
    expect(isNYSEOpen()).toBeTrue()
  })

  it('returns true exactly at 09:30', () => {
    mockET(1, 9, 30)
    expect(isNYSEOpen()).toBeTrue()
  })

  it('returns false before 09:30', () => {
    mockET(1, 9, 29)
    expect(isNYSEOpen()).toBeFalse()
  })

  it('returns false at 16:00 (market closed)', () => {
    mockET(1, 16, 0)
    expect(isNYSEOpen()).toBeFalse()
  })

  it('returns false after 16:00', () => {
    mockET(1, 16, 30)
    expect(isNYSEOpen()).toBeFalse()
  })

  it('returns false on Saturday', () => {
    mockET(6, 12, 0)
    expect(isNYSEOpen()).toBeFalse()
  })

  it('returns false on Sunday', () => {
    mockET(7, 12, 0)
    expect(isNYSEOpen()).toBeFalse()
  })
})

describe('secondsUntilNextOpen', () => {
  it('returns a positive number of seconds', () => {
    const seconds = secondsUntilNextOpen()
    expect(seconds).toBeGreaterThan(0)
  })

  it('never exceeds 4 days of seconds (handles weekend gap)', () => {
    const fourDaysInSeconds = 4 * 24 * 60 * 60
    const seconds = secondsUntilNextOpen()
    expect(seconds).toBeLessThanOrEqual(fourDaysInSeconds)
  })
})
