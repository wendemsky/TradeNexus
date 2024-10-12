package com.marshals.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.marshals.business.services.ClientService;

@WebMvcTest
class ClientControllerWebLayerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private ClientService mockService;
	

}
