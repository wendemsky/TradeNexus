import { Trade } from "src/app/models/trade";

export const mockTrades: Trade[] = [
    {
        "instrumentId": "N123456",
        "quantity": 10,
        "executionPrice": 104.25,
        "direction": "S",
        "clientId": "123",
        "order": {
            "instrumentId": "N123456",
            "quantity": 10,
            "targetPrice": 104,
            "direction": "S",
            "clientId": "123",
            "token": 920141621
        },
        "tradeId": "rfn2gsbg1t-qkar7iihgk-c1mh2nx218s",
        "cashValue": 1052.925
    }
]