-- Passwords: BCrypt hash of 'Marsh2024' (cost factor 12)
INSERT INTO client (client_id, email, password, name, date_of_birth, country, is_admin, curr_balance) VALUES
('1654658069', 'sowmya@gmail.com',     '$2b$12$Tpi8dE57kPXrq7PD9ZkVV.lHVx4AUzvba3bsnKnVAZmTfhTMgjFuC', 'Sowmya',    '2002-11-12', 'India', TRUE,  10000.0000),
('541107416',  'himanshu@gmail.com',   '$2b$12$ocfBvGk9Gc10Y9TK5ogCrOVGnHart1agqkq.OZHbG9bPRPWnLhStG', 'Himanshu',  '2002-12-08', 'India', TRUE,  10000.0000),
('1425922638', 'rishiyanth@gmail.com', '$2b$12$/eotYoPx8D.LlktxX7E6BOjTixz7J24/V1akyCB6sXX/Dc7WgvTKW', 'Rishiyanth','2002-11-08', 'India', TRUE,  10000.0000),
('1644724236', 'aditi@gmail.com',      '$2b$12$ocx0U6/w9.l8wQy8a4pIIOlQO53xzZ.RDOkAjqrbo6vQ15J2t6woG', 'Aditi',     '2002-09-28', 'India', TRUE,  10000.0000),
('1236679496', 'mohith@gmail.com',     '$2b$12$sd/ojych0scyIRqYE1TG/.BY49pSsakGmCCwf/8xJlK4yykXax2oi', 'Mohith',    '2002-06-25', 'India', TRUE,  10000.0000),
('739982664',  'john.doe@gmail.com',   '$2b$12$YUzb2DiJcetmCeh8/R/jl.EfYFWYXE3xZROyW7mLKQqiVeEZdGAWi', 'John Doe',  '1998-03-15', 'USA',   FALSE, 10000.0000);

INSERT INTO client_identification (client_id, type, value) VALUES
('1654658069', 'Aadhar', '123456789102'),
('541107416',  'Aadhar', '154365349453'),
('1425922638', 'Aadhar', '784304248242'),
('1644724236', 'Aadhar', '759304541346'),
('1236679496', 'Aadhar', '674902441257'),
('739982664',  'SSN',    '123457353');

INSERT INTO client_preferences (client_id, investment_purpose, income_category, length_of_investment, percentage_of_spend, risk_tolerance, accept_advisor) VALUES
('1654658069', 'Education',     'HIG',  'Short',  'Tier4', 2, FALSE),
('541107416',  'Major Expense', 'LIG',  'Medium', 'Tier2', 1, TRUE),
('1425922638', 'Education',     'MIG',  'Short',  'Tier1', 5, TRUE),
('1644724236', 'Retirement',    'VHIG', 'Medium', 'Tier4', 3, FALSE),
('1236679496', 'Retirement',    'MIG',  'Long',   'Tier3', 3, TRUE),
('739982664',  'Retirement',    'LIG',  'Short',  'Tier4', 1, TRUE);
