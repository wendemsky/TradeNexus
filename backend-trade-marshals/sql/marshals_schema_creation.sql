 DROP TABLE CLIENT_PREFERENCES;
 DROP TABLE CLIENT_IDENTIFICATION;
 DROP TABLE CLIENT;
 DROP TABLE client_portfolio;
 DROP TABLE holdings;
 
 //Create Table Client
 CREATE TABLE "MARSH"."CLIENT" (	
    client_id VARCHAR2(50),
    email VARCHAR2(50) UNIQUE NOT NULL,
    password VARCHAR2(255) NOT NULL,
    name VARCHAR2(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    country VARCHAR2(50) NOT NULL,
    is_admin CHAR(1) NOT NULL,
    curr_balance NUMBER NOT NULL,
    CONSTRAINT pk_client_id PRIMARY KEY(client_id),
    
    CONSTRAINT chk_is_admin CHECK (is_admin IN ('Y', 'N'))
);

//CONSTRAINT email_format CHECK (REGEXP_LIKE(email, '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$')),
 //Create Table Client Identification
 CREATE TABLE "MARSH"."CLIENT_IDENTIFICATION" (
    client_id VARCHAR2(50),
    type VARCHAR2(255),
    value VARCHAR2(255),
    CONSTRAINT pk_client_id_type PRIMARY KEY(client_id,type),
    CONSTRAINT fk_client_id_identification FOREIGN KEY(client_id) REFERENCES CLIENT(client_id)
 );
 
 
 //Populate 5 Admin values and 1 non admin client value
 //Initializing each client with $10k balance
 INSERT INTO client (client_id, email, password, name, date_of_birth, country, is_admin, curr_balance) 
 VALUES ('1654658069', 'sowmya@gmail.com', 'Marsh2024', 'Sowmya', to_date('2002-11-12', 'yyyy-mm-dd'), 'India', 'Y', 10000);
 INSERT INTO client_identification (client_id, type, value)
 VALUES ('1654658069','Aadhar','123456789102');
 
 INSERT INTO client (client_id, email, password, name, date_of_birth, country, is_admin, curr_balance) 
 VALUES ('541107416', 'himanshu@gmail.com', 'Marsh2024', 'Himanshu', to_date('2002-12-08', 'yyyy-mm-dd'), 'India', 'Y', 10000);
 INSERT INTO client_identification (client_id, type, value)
 VALUES ('541107416','Aadhar','154365349453');
 
 INSERT INTO client (client_id, email, password, name, date_of_birth, country, is_admin, curr_balance) 
 VALUES ('1425922638', 'rishiyanth@gmail.com', 'Marsh2024', 'Rishiyanth', to_date('2002-11-08', 'yyyy-mm-dd'), 'India', 'Y', 10000);
 INSERT INTO client_identification (client_id, type, value)
 VALUES ('1425922638','Aadhar','784304248242');
 
 INSERT INTO client (client_id, email, password, name, date_of_birth, country, is_admin, curr_balance) 
 VALUES ('1644724236', 'aditi@gmail.com', 'Marsh2024', 'Aditi', to_date('2002-09-28', 'yyyy-mm-dd'), 'India', 'Y', 10000);
 INSERT INTO client_identification (client_id, type, value)
 VALUES ('1644724236','Aadhar','759304541346');
 
 INSERT INTO client (client_id, email, password, name, date_of_birth, country, is_admin, curr_balance) 
 VALUES ('1236679496', 'mohith@gmail.com', 'Marsh2024', 'Mohith', to_date('2002-06-25', 'yyyy-mm-dd'), 'India', 'Y', 10000);
 INSERT INTO client_identification (client_id, type, value)
 VALUES ('1236679496','Aadhar','674902441257');
 
 INSERT INTO client (client_id, email, password, name, date_of_birth, country, is_admin, curr_balance) 
 VALUES ('739982664', 'john.doe@gmail.com', 'Marsh2024', 'John Doe', to_date('1998-03-15', 'yyyy-mm-dd'), 'USA', 'N', 10000);
 INSERT INTO client_identification (client_id, type, value)
 VALUES ('739982664','SSN','1234573532');
 
 // Create table for Client Preferences
 CREATE TABLE "MARSH"."CLIENT_PREFERENCES" (
    client_id VARCHAR2(50),
    investment_purpose VARCHAR2(30),
    income_category VARCHAR2(6),
    length_of_investment VARCHAR2(15),
    percentage_of_spend VARCHAR2(6),
    risk_tolerance NUMBER(2),
    is_advisor_accepted VARCHAR2(5),
    CONSTRAINT pk_client_id_type_preferences PRIMARY KEY(client_id),
    CONSTRAINT fk_client_id_preferences FOREIGN KEY(client_id) REFERENCES CLIENT(client_id),
    CONSTRAINT chk_is_advisor_accepted CHECK (is_advisor_accepted IN ('true', 'false'))
 );
 
 //client portfolio table
 CREATE TABLE client_portfolio (
    client_id VARCHAR(255) PRIMARY KEY,
    curr_balance DECIMAL(19, 4) NOT NULL
);

//holdings table
CREATE TABLE holdings (
    client_id VARCHAR(255) NOT NULL,
    instrument_id VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    avg_price DECIMAL(19, 4) NOT NULL,
    FOREIGN KEY (client_id) REFERENCES client_portfolio(client_id) ON DELETE CASCADE
);
 
//Inserting 6 records into Client Preferences table
//Two records will have accept advisor set to false
INSERT INTO CLIENT_PREFERENCES (client_id ,investment_purpose, income_category, length_of_investment, percentage_of_spend, risk_tolerance, is_advisor_accepted) 
VALUES ( '1654658069', 'Education', 'HIG', 'Short', 'Tier4', 2, 'false');

INSERT INTO CLIENT_PREFERENCES (client_id ,investment_purpose, income_category, length_of_investment, percentage_of_spend, risk_tolerance, is_advisor_accepted) 
VALUES ( '541107416', 'Major Expense', 'LIG', 'Medium', 'Tier2', 1, 'true');

INSERT INTO CLIENT_PREFERENCES (client_id ,investment_purpose, income_category, length_of_investment, percentage_of_spend, risk_tolerance, is_advisor_accepted) 
VALUES ( '1425922638', 'Education', 'MIG', 'Short', 'Tier1', 5, 'true');

INSERT INTO CLIENT_PREFERENCES (client_id ,investment_purpose, income_category, length_of_investment, percentage_of_spend, risk_tolerance, is_advisor_accepted) 
VALUES ( '1644724236', 'Retirement', 'VHIG', 'Medium', 'Tier4', 3, 'false');

INSERT INTO CLIENT_PREFERENCES (client_id ,investment_purpose, income_category, length_of_investment, percentage_of_spend, risk_tolerance, is_advisor_accepted) 
VALUES ( '1236679496', 'Retirement', 'MIG', 'Long', 'Tier3', 3, 'true');

INSERT INTO CLIENT_PREFERENCES (client_id ,investment_purpose, income_category, length_of_investment, percentage_of_spend, risk_tolerance, is_advisor_accepted) 
VALUES ( '739982664', 'Retirement', 'LIG', 'Short', 'Tier4', 1, 'true'); 

COMMIT;
