package com.marshals.restcontroller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.http.ResponseEntity.HeadersBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.marshals.business.ClientPreferences;
import com.marshals.business.services.ClientPreferencesService;
import com.marshals.integration.DatabaseException;

@RestController
@RequestMapping("/client-preferences")
public class ClientPreferencesController {
	
	@Autowired
	private Logger logger;
	
	@Autowired
	private ClientPreferencesService clientPreferencesService;

	@GetMapping(value="/ping")
	public String ping() {
		return "Client Preferences web service is alive at " + LocalDateTime.now();
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ClientPreferences> getClientPreferencesById(@PathVariable String id){
		ResponseEntity<ClientPreferences> response = null;
		try {
			if(Long.parseLong(id) <= 0) {
				throw new IllegalArgumentException("Invalid format for client id");
			}
			ClientPreferences clientPreferences = clientPreferencesService.getClientPreferences(id);
			response = ResponseEntity.ok(clientPreferences);
		}
		catch(IllegalArgumentException e) {
			logger.error("Error in request for getting client preferences", e);
			response = ResponseEntity.badRequest().build();
		}
		catch(DatabaseException e) {
			logger.error("Error in fetching data", e);
			response = ResponseEntity.noContent().build();
		}
		catch(RuntimeException e) {
			logger.error("Problem occured from server", e);
			response = ResponseEntity.internalServerError().build();
		}
		return response;
	}
	
	@PostMapping()
	public ResponseEntity<ClientPreferences> addClientPreferences(@RequestBody ClientPreferences clientPreferences){
		ResponseEntity<ClientPreferences> response = null;
		try {
			if(clientPreferences == null) {
				throw new NullPointerException("Client Preferences request body is null");
			}
			if(clientPreferencesService.addClientPreferences(clientPreferences)) {
				response = ResponseEntity.ok(clientPreferences);
			}
		}
		catch(NullPointerException e) {
			logger.error("Error in request for adding client preferences", e);
			response = ResponseEntity.badRequest().build();
		}
		catch(DatabaseException e) {
			logger.error("Error inserting data", e);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
		}
		catch(RuntimeException e) {
			logger.error("Problem occured from server", e);
			response = ResponseEntity.internalServerError().build();
		}
		return response;
	}
	
	@PutMapping()
	public ResponseEntity<ClientPreferences> updateClientPreferences(@RequestBody ClientPreferences clientPreferences){
		ResponseEntity<ClientPreferences> response = null;
		try {
			if(clientPreferences == null) {
				throw new NullPointerException("Client Preferences request body is null");
			}
			if(clientPreferencesService.updateClientPreferences(clientPreferences)) {
				response = ResponseEntity.ok(clientPreferences);
			}
		}
		catch(NullPointerException e) {
			logger.error("Error in request for updating client preferences", e);
			response = ResponseEntity.badRequest().build();
		}
		catch(DatabaseException e) {
			logger.error("Error updating data", e);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
		}
		catch(RuntimeException e) {
			logger.error("Problem occured from server", e);
			response = ResponseEntity.internalServerError().build();
		}
		return response;
	}
	
}
