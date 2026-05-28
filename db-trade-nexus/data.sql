-- Populate 5 Admin values and 1 non-admin client value
-- Initializing each client with $10k balance
INSERT INTO CLIENT (client_id, email, password, name, date_of_birth, country, is_admin, curr_balance) 
VALUES ('1654658069', 'sowmya@gmail.com', 'Marsh2024', 'Sowmya', '2002-11-12 00:00:00', 'India', 'Y', 10000);
INSERT INTO CLIENT_IDENTIFICATION (client_id, type, value)
VALUES ('1654658069','Aadhar','123456789102');
INSERT INTO CLIENT (client_id, email, password, name, date_of_birth, country, is_admin, curr_balance) 
VALUES ('541107416', 'himanshu@gmail.com', 'Marsh2024', 'Himanshu', '2002-12-08 00:00:00', 'India', 'Y', 10000);
INSERT INTO CLIENT_IDENTIFICATION (client_id, type, value)
VALUES ('541107416','Aadhar','154365349453');
INSERT INTO CLIENT (client_id, email, password, name, date_of_birth, country, is_admin, curr_balance) 
VALUES ('1425922638', 'rishiyanth@gmail.com', 'Marsh2024', 'Rishiyanth', '2002-11-08 00:00:00', 'India', 'Y', 10000);
INSERT INTO CLIENT_IDENTIFICATION (client_id, type, value)
VALUES ('1425922638','Aadhar','784304248242');
INSERT INTO CLIENT (client_id, email, password, name, date_of_birth, country, is_admin, curr_balance) 
VALUES ('1644724236', 'aditi@gmail.com', 'Marsh2024', 'Aditi', '2002-09-28 00:00:00', 'India', 'Y', 10000);
INSERT INTO CLIENT_IDENTIFICATION (client_id, type, value)
VALUES ('1644724236','Aadhar','759304541346');
INSERT INTO CLIENT (client_id, email, password, name, date_of_birth, country, is_admin, curr_balance) 
VALUES ('1236679496', 'mohith@gmail.com', 'Marsh2024', 'Mohith', '2002-06-25 00:00:00', 'India', 'Y', 10000);
INSERT INTO CLIENT_IDENTIFICATION (client_id, type, value)
VALUES ('1236679496','Aadhar','674902441257');
INSERT INTO CLIENT (client_id, email, password, name, date_of_birth, country, is_admin, curr_balance) 
VALUES ('739982664', 'john.doe@gmail.com', 'Marsh2024', 'John Doe', '1998-03-15 00:00:00', 'USA', 'N', 10000);
INSERT INTO CLIENT_IDENTIFICATION (client_id, type, value)
VALUES ('739982664','SSN','123457353');

-- Inserting 6 records into Client Preferences table
INSERT INTO CLIENT_PREFERENCES (client_id ,investment_purpose, income_category, length_of_investment, percentage_of_spend, risk_tolerance, is_advisor_accepted) 
VALUES ('1654658069', 'Education', 'HIG', 'Short', 'Tier4', 2, 'false');
INSERT INTO CLIENT_PREFERENCES (client_id ,investment_purpose, income_category, length_of_investment, percentage_of_spend, risk_tolerance, is_advisor_accepted) 
VALUES ('541107416', 'Major Expense', 'LIG', 'Medium', 'Tier2', 1, 'true');
INSERT INTO CLIENT_PREFERENCES (client_id ,investment_purpose, income_category, length_of_investment, percentage_of_spend, risk_tolerance, is_advisor_accepted) 
VALUES ('1425922638', 'Education', 'MIG', 'Short', 'Tier1', 5, 'true');
INSERT INTO CLIENT_PREFERENCES (client_id ,investment_purpose, income_category, length_of_investment, percentage_of_spend, risk_tolerance, is_advisor_accepted) 
VALUES ('1644724236', 'Retirement', 'VHIG', 'Medium', 'Tier4', 3, 'false');
INSERT INTO CLIENT_PREFERENCES (client_id ,investment_purpose, income_category, length_of_investment, percentage_of_spend, risk_tolerance, is_advisor_accepted) 
VALUES ('1236679496', 'Retirement', 'MIG', 'Long', 'Tier3', 3, 'true');
INSERT INTO CLIENT_PREFERENCES (client_id ,investment_purpose, income_category, length_of_investment, percentage_of_spend, risk_tolerance, is_advisor_accepted) 
VALUES ('739982664', 'Retirement', 'LIG', 'Short', 'Tier4', 1, 'true');


