package com.fidelity.fmts;

import java.math.BigDecimal;
import java.util.Objects;

public class ValidatedClient {
	
	private String email;
	private String clientId;
	private BigDecimal token;
	
	public ValidatedClient(String email, String clientId, BigDecimal token) {
		try {
			if(email==null || clientId==null || token == null) 
				throw new NullPointerException("Validated Client Details cannot be null");
			this.email = email;
			this.clientId = clientId;
			this.token = token;
		} catch(NullPointerException e) {
			throw e;
		}
	
	}
	
	public String getClientId() {
		return clientId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(clientId, email, token);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValidatedClient other = (ValidatedClient) obj;
		return Objects.equals(clientId, other.clientId) && Objects.equals(email, other.email)
				&& Objects.equals(token, other.token);
	}
	
}
