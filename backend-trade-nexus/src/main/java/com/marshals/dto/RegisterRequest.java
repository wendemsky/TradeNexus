package com.marshals.dto;

import java.util.List;

public class RegisterRequest {

    private String email;
    private String password;
    private String name;
    private String dateOfBirth;
    private String country;
    private List<IdentificationEntry> identification;

    public RegisterRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public List<IdentificationEntry> getIdentification() { return identification; }
    public void setIdentification(List<IdentificationEntry> identification) { this.identification = identification; }

    public static class IdentificationEntry {
        private String type;
        private String value;

        public IdentificationEntry() {}

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}
