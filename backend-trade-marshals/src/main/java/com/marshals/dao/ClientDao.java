package com.marshals.dao;

import java.util.List;

import com.marshals.models.Client;
import com.marshals.models.ClientIdentification;
import com.marshals.models.ClientPortfolio;

//Has functions related to Client related data access
public interface ClientDao {

	Boolean verifyClientEmail(String email); // Email Validation

	Client getClientAtLogin(String email, String password); // For login

	List<ClientIdentification> getAllClientIdentificationDetails(); // Required for ID validation of new client

	void addNewClient(Client client, ClientPortfolio clientPortfolio); // For register

}
