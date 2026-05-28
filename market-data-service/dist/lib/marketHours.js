"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.isNYSEOpen = isNYSEOpen;
exports.secondsUntilNextOpen = secondsUntilNextOpen;
const luxon_1 = require("luxon");
const TZ = 'America/New_York';
const OPEN_HOUR = 9;
const OPEN_MINUTE = 30;
const CLOSE_HOUR = 16;
function isNYSEOpen() {
    const now = luxon_1.DateTime.now().setZone(TZ);
    const day = now.weekday;
    if (day >= 6)
        return false;
    const afterOpen = now.hour > OPEN_HOUR || (now.hour === OPEN_HOUR && now.minute >= OPEN_MINUTE);
    const beforeClose = now.hour < CLOSE_HOUR;
    return afterOpen && beforeClose;
}
function secondsUntilNextOpen() {
    const now = luxon_1.DateTime.now().setZone(TZ);
    let next = now.set({ hour: OPEN_HOUR, minute: OPEN_MINUTE, second: 0, millisecond: 0 });
    if (next <= now) {
        next = next.plus({ days: 1 });
    }
    // Skip to Monday if next open lands on weekend
    while (next.weekday >= 6) {
        next = next.plus({ days: 1 });
    }
    return Math.ceil(next.diff(now, 'seconds').seconds);
}
