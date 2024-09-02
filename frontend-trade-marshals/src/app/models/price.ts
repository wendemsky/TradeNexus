import { Instrument } from "./instrument";

export class Price {
    constructor(
        public askPrice: number,
        public bidPrice: number,
        public priceTimestamp: string,
        public instrument: Instrument
    ) {}
}


// {
//     "askPrice": 104.75,
//     "bidPrice": 104.25,
//     "priceTimestamp": "21-AUG-19 10.00.01.042000000 AM GMT",
//     "instrument": {
//         "instrumentId": "N123456",
//         "externalIdType": "CUSIP",
//         "externalId": "46625H100",
//         "categoryId": "STOCK",
//         "instrumentDescription": "JPMorgan Chase & Co. Capital Stock",
//         "maxQuantity": 1000,
//         "minQuantity": 1
//     }
// }