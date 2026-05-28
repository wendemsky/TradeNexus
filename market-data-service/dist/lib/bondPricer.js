"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.bondPrice = bondPrice;
function bondPrice(faceValue, couponRate, yieldRate, yearsToMaturity, paymentsPerYear = 2) {
    const c = (couponRate * faceValue) / paymentsPerYear;
    const y = yieldRate / paymentsPerYear;
    const n = Math.round(yearsToMaturity * paymentsPerYear);
    if (n <= 0)
        return faceValue;
    const pvCoupons = c * (1 - Math.pow(1 + y, -n)) / y;
    const pvPar = faceValue / Math.pow(1 + y, n);
    return Math.round((pvCoupons + pvPar) * 10000) / 10000;
}
