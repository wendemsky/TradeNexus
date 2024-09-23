DROP TABLE holdings;
DROP TABLE CLIENT_TRADE;
DROP TABLE CLIENT_ORDER;
DROP TABLE CLIENT_PREFERENCES;
DROP TABLE CLIENT_IDENTIFICATION;
DROP TABLE CLIENT;
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
    CONSTRAINT chk_email_format CHECK (REGEXP_LIKE(email, '^[A-Za-z0-9\._%+-]+@[A-Za-z0-9\.-]+\.[A-Za-z]{2,}$')),
    CONSTRAINT chk_is_admin CHECK (is_admin IN ('Y', 'N'))
);
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
 
//Create holdings table
CREATE TABLE holdings (
    client_id VARCHAR(255) NOT NULL,
    instrument_id VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    avg_price DECIMAL(19, 4) NOT NULL,
    FOREIGN KEY (client_id) REFERENCES client(client_id) ON DELETE CASCADE
);
 
--Create table ORDER
 
CREATE TABLE "MARSH"."CLIENT_ORDER"(
    instrument_id VARCHAR2(50) NOT NULL,
    quantity NUMBER NOT NULL,
    target_price DECIMAL(25,10) NOT NULL,
    direction CHAR(1) NOT NULL,
    client_id VARCHAR2(50),
    order_id VARCHAR2(50),
    token NUMBER NOT NULL,
    CONSTRAINT pk_order_id PRIMARY KEY(order_id),
    CONSTRAINT chk_direction CHECK (direction IN('S', 'B')),
--    FOREIGN KEY(client_id) REFERENCES CLIENT(client_id) ON DELETE CASCADE
    CONSTRAINT fk_order FOREIGN KEY(client_id) REFERENCES CLIENT(client_id) ON DELETE CASCADE
);
 
--Insert 10 values into ORDER table
 
INSERT INTO "MARSH"."CLIENT_ORDER" (instrument_id, quantity, target_price, direction, client_id, order_id, token) VALUES
('N123456', 1000, 104.75, 'B', '1654658069', 'ORDER001', 1);
INSERT INTO "MARSH"."CLIENT_ORDER" (instrument_id, quantity, target_price, direction, client_id, order_id, token) VALUES
('N123789', 10, 312500, 'S', '1654658069', 'ORDER002', 2);
INSERT INTO "MARSH"."CLIENT_ORDER" (instrument_id, quantity, target_price, direction, client_id, order_id, token) VALUES
('C100', 1000, 95.92, 'B', '541107416', 'ORDER003', 3);
INSERT INTO "MARSH"."CLIENT_ORDER" (instrument_id, quantity, target_price, direction, client_id, order_id, token) VALUES
('T67890', 10000, 1.03375, 'S', '541107416', 'ORDER004', 4);
INSERT INTO "MARSH"."CLIENT_ORDER" (instrument_id, quantity, target_price, direction, client_id, order_id, token) VALUES
('T67894', 10000, 0.998125, 'S', '1425922638', 'ORDER005', 5);
INSERT INTO "MARSH"."CLIENT_ORDER" (instrument_id, quantity, target_price, direction, client_id, order_id, token) VALUES
('T67895', 10000, 1, 'B', '1425922638', 'ORDER006', 6);
INSERT INTO "MARSH"."CLIENT_ORDER" (instrument_id, quantity, target_price, direction, client_id, order_id, token) VALUES
('T67897', 10000, 0.999375, 'B', '1644724236', 'ORDER007', 7);
INSERT INTO "MARSH"."CLIENT_ORDER" (instrument_id, quantity, target_price, direction, client_id, order_id, token) VALUES
('T67899', 10000, 0.999375, 'B', '1644724236', 'ORDER008', 8);
INSERT INTO "MARSH"."CLIENT_ORDER" (instrument_id, quantity, target_price, direction, client_id, order_id, token) VALUES
('T67880', 10000, 1.00375, 'S', '1236679496', 'ORDER009', 9);
INSERT INTO "MARSH"."CLIENT_ORDER" (instrument_id, quantity, target_price, direction, client_id, order_id, token) VALUES
('T67883', 10000, 1.0596875, 'S', '1236679496', 'ORDER010', 10);
 
--Create table TRADE
 
CREATE TABLE "MARSH"."CLIENT_TRADE" (
    trade_id VARCHAR2(50),
    order_id VARCHAR2(50),
    execution_price DECIMAL(25,10) NOT NULL,
    cash_value DECIMAL(25,10) NOT NULL,
    CONSTRAINT pk_trade_id PRIMARY KEY(trade_id),
--    FOREIGN KEY(order_id) REFERENCES CLIENT_ORDER(order_id) ON DELETE CASCADE
    CONSTRAINT fk_trade FOREIGN KEY(order_id) REFERENCES CLIENT_ORDER(order_id) ON DELETE CASCADE
);
 
--Insert 10 values into Trade table
 
INSERT INTO "MARSH"."CLIENT_TRADE" (trade_id, order_id, execution_price, cash_value) VALUES
('TRADE001', 'ORDER001', 104.75, 104750.00);
INSERT INTO "MARSH"."CLIENT_TRADE" (trade_id, order_id, execution_price, cash_value) VALUES
('TRADE002', 'ORDER002', 312500, 3125000.00);
INSERT INTO "MARSH"."CLIENT_TRADE" (trade_id, order_id, execution_price, cash_value) VALUES
('TRADE003', 'ORDER003', 95.92, 95920.00);
INSERT INTO "MARSH"."CLIENT_TRADE" (trade_id, order_id, execution_price, cash_value) VALUES
('TRADE004', 'ORDER004', 1.03375, 10337.50);
INSERT INTO "MARSH"."CLIENT_TRADE" (trade_id, order_id, execution_price, cash_value) VALUES
('TRADE005', 'ORDER005', 0.998125, 9981.25);
INSERT INTO "MARSH"."CLIENT_TRADE" (trade_id, order_id, execution_price, cash_value) VALUES
('TRADE006', 'ORDER006', 1, 10000.00);
INSERT INTO "MARSH"."CLIENT_TRADE" (trade_id, order_id, execution_price, cash_value) VALUES
('TRADE007', 'ORDER007', 0.999375, 9993.75);
INSERT INTO "MARSH"."CLIENT_TRADE" (trade_id, order_id, execution_price, cash_value) VALUES
('TRADE008', 'ORDER008', 0.999375, 9993.75);
INSERT INTO "MARSH"."CLIENT_TRADE" (trade_id, order_id, execution_price, cash_value) VALUES
('TRADE009', 'ORDER009', 1.00375, 10037.50);
INSERT INTO "MARSH"."CLIENT_TRADE" (trade_id, order_id, execution_price, cash_value) VALUES
('TRADE010', 'ORDER010', 1.0596875, 10596.88);
 
COMMIT;