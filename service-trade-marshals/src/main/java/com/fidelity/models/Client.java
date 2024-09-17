package com.fidelity.models;

import java.util.List;
import java.util.Objects;

public class Client {
	
	private String email;
	private String clientId;
	private String password;
	
	private String name;
	private String dateOfBirth;
	private String country;
	private List<ClientIdentification> identification;
	
	private boolean isAdmin;

	public Client(String email, String clientId, String password, String name, String dateOfBirth, String country,
			List<ClientIdentification> identification, boolean isAdmin) {
		try {
			if(email==null || clientId==null || password==null || name==null 
					||dateOfBirth==null || country==null || identification==null) 
				throw new NullPointerException("Client Details cannot be null");
			//Only checking validity of fields that are not explicitly covered in Angular
			if(country!="India" && country!="USA")
				throw new IllegalArgumentException("Country not covered");
			this.email = email;
			this.clientId = clientId;
			this.password = password;
			this.name = name;
			this.dateOfBirth = dateOfBirth;
			this.country = country;
			this.identification = identification;
			this.isAdmin = isAdmin;
		}  catch(NullPointerException e) {
			throw e;
		} catch(IllegalArgumentException e) {
			throw e;
		}
		
	}
	
	
	//Getters
	public String getEmail() {
		return email;
	}

	public String getClientId() {
		return clientId;
	}
	
	public String getPassword() {
		return password;
	}
	
	public List<ClientIdentification> getIdentificationDetails() {
		return identification;
	}

	@Override
	public int hashCode() {
		return Objects.hash(clientId, country, dateOfBirth, email, identification, isAdmin, name, password);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Client other = (Client) obj;
		return Objects.equals(clientId, other.clientId) && Objects.equals(country, other.country)
				&& Objects.equals(dateOfBirth, other.dateOfBirth) && Objects.equals(email, other.email)
				&& Objects.equals(identification, other.identification) && isAdmin == other.isAdmin
				&& Objects.equals(name, other.name) && Objects.equals(password, other.password);
	}

	@Override
	public String toString() {
		return "Client [email=" + email + ", clientId=" + clientId + ", password=" + password + ", name=" + name
				+ ", dateOfBirth=" + dateOfBirth + ", country=" + country + ", identification=" + identification
				+ ", isAdmin=" + isAdmin + "]";
	}
	
	
}
