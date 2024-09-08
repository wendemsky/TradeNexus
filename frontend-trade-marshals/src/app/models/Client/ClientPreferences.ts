export class ClientPreferences {
    find(arg0: (client: any) => boolean): any {
      throw new Error('Method not implemented.')
    }
    clientId?: string
    investmentPurpose?: string
    incomeCategory?: string
    lengthOfInvestment?: string
    percentageOfSpend?: string
    riskTolerance?: number
    acceptAdvisor?: string

    constructor(){
        this.clientId = ""
        this.investmentPurpose = ""
        this.incomeCategory = ""
        this.lengthOfInvestment = ""
        this.percentageOfSpend = ""
        this.riskTolerance = 1
        this.acceptAdvisor = ""
    }
}