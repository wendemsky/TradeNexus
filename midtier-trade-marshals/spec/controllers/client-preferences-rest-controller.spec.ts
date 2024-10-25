import defaultConfig from '../../src/constants';
import { ClientPreferences } from '../../src/models/Client/ClientPreferences';
import { ClientPreferencesRestController } from '../../src/controllers/client-preferences-rest-controller';

const testClientPreferences: ClientPreferences = {
	"clientId": "811820801",
	"investmentPurpose": "Education",
	"incomeCategory": "MIG",
	"lengthOfInvestment": "Short",
	"percentageOfSpend": "Tier1",
	"riskTolerance": 5,
	"acceptAdvisor": "true"
}

let backendUrl: string = `${defaultConfig.BACKEND_URL}client-preferences`

describe('Client Preferences Rest Controller Unit Tests', () => {
    let controller: ClientPreferencesRestController
    let mockAxiosGet: any
    let mockAxiosPost: any
    let mockBackendResponse: any
    let mockHttpRequest: Partial<Request>;
    let mockHttpResponse: Partial<Response>;

    beforeEach(() => {
        controller = new ClientPreferencesRestController();
        mockAxiosGet = spyOn(axios, 'get').and.callThrough();
        mockAxiosPost = spyOn(axios, 'post').and.callThrough();
        mockHttpResponse = {
            status: jasmine.createSpy('status').and.callFake(() => mockHttpResponse), // Mock status method
            json: jasmine.createSpy('json'), send: jasmine.createSpy('send')
        };
    });

    describe('Get Client Preferences', () => {
        it('returns success 200 when ')
    })
})