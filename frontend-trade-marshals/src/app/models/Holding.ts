export interface Holding {
    categoryId: string,
    instrumentId: string,
    instrumentDesc: string,
    quantity: number,
    avgPrice: number //Based on trade (exec Price) you 
}