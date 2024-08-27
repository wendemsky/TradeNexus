import { ClientIdentification } from "./ClientIdentification";

export interface Client{
    email: string,
    clientId: string,
    password: string,
    dateOfBirth: string,
    country: string,
    postalCode: string,
    identification: ClientIdentification[]
}