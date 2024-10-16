import { ClientIdentification } from "./ClientIdentification";

export interface Client{
    email: string,
    clientId?: string | undefined,
    password: string,
    name: string,
    dateOfBirth: string,
    country: string,
    identification: ClientIdentification[],
    isAdmin?: boolean
}