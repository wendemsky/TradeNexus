package com.marshals.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.marshals.business.services.PortfolioService;

class PortfolioControllerWebLayerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private PortfolioService mockService;

}
