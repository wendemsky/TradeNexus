export interface Holding {
    categoryId: string,
    instrumentId: string,
    instrumentDesc: string,
    quantity: number,
    currPrice: number //Based on trade (exec Price) you add or remove to it
}