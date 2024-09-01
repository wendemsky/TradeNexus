import { Holding } from "../Holding";

export interface ClientPortfolio {
    clientId: string | undefined,
    currBalance: number,
    holdings: Holding[]
}