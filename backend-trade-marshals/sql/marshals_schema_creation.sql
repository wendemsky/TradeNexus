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
 
