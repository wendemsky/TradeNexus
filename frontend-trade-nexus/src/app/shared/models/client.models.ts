export interface ClientIdentification {
  type: 'Aadhar' | 'PAN' | 'SSN';
  value: string;
}

export interface Client {
  email: string;
  clientId: string;
  name: string;
  dateOfBirth: string;
  country: 'India' | 'USA';
  identification: ClientIdentification[];
  isAdmin: boolean;
}

export interface ClientProfile {
  client: Client;
  token: string;
}

export interface ClientPortfolio {
  clientId: string;
  currBalance: number;
  holdings: Holding[];
}

export interface Holding {
  instrumentId: string;
  instrumentDescription: string;
  categoryId: string;
  quantity: number;
  avgPrice: number;
}

export interface HoldingWithPL extends Holding {
  currentBidPrice: number;
  unrealizedPL: number;
  unrealizedPLPct: number;
}

export interface ClientPreferences {
  clientId: string;
  investmentPurpose: 'Education' | 'Major Expense' | 'Retirement';
  incomeCategory: 'LIG' | 'MIG' | 'HIG' | 'VHIG';
  lengthOfInvestment: 'Short' | 'Medium' | 'Long';
  percentageOfSpend: 'Tier1' | 'Tier2' | 'Tier3' | 'Tier4';
  riskTolerance: number;
  acceptAdvisor: boolean;
}

export interface IsVerifiedClient {
  isVerified: boolean;
}
