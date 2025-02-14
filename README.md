# Trade Nexus
Trade Nexus is a mock trading platform designed to simulate a realistic trading experience. 

The platform is built with a robust architecture featuring five distinct layers:

1. **Database**: SQLite3 database for data storage and retrieval.
2. **Frontend**: Developed using Angular for a dynamic and responsive user interface.
3. **Mid-Tier**: Built with Node.js for seamless communication between the frontend and backend.
4. **Backend**: Implemented in Java Spring Boot to handle core business logic and services.
5. **FIPS-Service**: A dedicated Financial Instruments Pricing Service for live pricing and client token management.

---

## Features

### Landing Page
- **Sign Up and Login**: Entry point for users to create an account or log in.

### Post-Login Pages
1. **Home**: Displays all available financial instruments for trading (buy/sell).
2. **Client Preferences**: Allows users to set preferences for the Robo Advisor.
3. **Portfolio**: Lists the user's holdings, with options to buy and sell directly from this page.
4. **Trading History**: A comprehensive history of all executed trades.
5. **Activity Report**:
   - Admins can download reports for any user.
   - Users can download their own reports.
   - Three types of reports are available (downloadable as Excel files):
     - Holdings Report
     - Profit and Loss (P&L) Report
     - Trade Report

### Robo Advisor
- Provides recommendations for the top 5 buy or sell opportunities based on the user's preferences.

### FIPS-Service (Financial Instruments Pricing Service)
1. **Live Pricing**:
   - Prices adjust dynamically whenever a trade is executed on the platform.
2. **Client Verification and Token Management**:
   - Verifies client credentials during login.
   - Issues a token valid for 30 minutes.
   - Requires token refresh for continued trading after expiration.

---

## How to Run

### Prerequisites
- Node.js and npm installed
- Angular CLI installed
- Java and Spring Boot environment configured

### Steps to Start the Platform

1. **Service and Mid-Tier**:
   ```bash
   npm start
   ```

2. **Frontend**:
   ```bash
   ng serve
   ```

3. **Backend**:
   Run the application as a Spring Boot app. You can use your IDE or execute the following command:
   ```bash
   ./mvnw spring-boot:run
   ```

---

## Screenshots

### Landing Page
![Landing Page](https://github.com/user-attachments/assets/28feb542-b2d4-40b3-b1ae-96f5f1b08717)
![Landing Page Features](https://github.com/user-attachments/assets/f62a9534-21a6-4bc3-8ef2-85a0087840e4)

#### Register New User
![Landing Page Register](https://github.com/user-attachments/assets/00b3af86-6955-4750-a582-88364f6fd3c1)

#### Personal Details
![Landing Page Register Personal Details](https://github.com/user-attachments/assets/5026f44a-b87a-40cc-8187-84c93a91699b)

#### Identification
![Landing Page Register Identification](https://github.com/user-attachments/assets/4da53c21-0cd3-4f18-a41c-24d35b82072d)


### Home Page
![Home Page](https://github.com/user-attachments/assets/373598a0-20ed-4870-8c7c-9437e8da5a91)

#### Buy or Sell
![Home Page Buy](https://github.com/user-attachments/assets/5c4af566-e38c-4051-a23e-d32e6b0e9988)


### Client Preferences
![Home Page Client Preferences](https://github.com/user-attachments/assets/e6d39b14-33cf-41bf-9c15-ccb6bd9bf42b)


### Portfolio
![Portfolio Page](https://github.com/user-attachments/assets/6ab2c7b1-4f02-4251-9b75-78ce603a64af)


### Trading History
![Trade History Page](https://github.com/user-attachments/assets/7653ab16-02dd-4ebb-95dd-c7a8b82631e7)


### Activity Report
![Client Activity Report Page](https://github.com/user-attachments/assets/67a714cf-52ba-4420-9643-59fc286ba343)


### Robo Advisor
![Robo Advisor](https://github.com/user-attachments/assets/3903100f-db00-4eb3-b081-e013956906ae)

---

## Technical Architecture

**Database**: SQLite3  
**Frontend**: Angular  
**Mid-Tier**: Node.js  
**Backend**: Java Spring Boot  
**FIPS-Service**: Node.js for live pricing and client token management

---

## Future Enhancements
- Hosting the platform for broader accessibility.
- Extending the Robo Advisor's capabilities with advanced analytics.
- Adding multi-language support.

---

## Contact
For any questions or suggestions, please reach out to the developer.

