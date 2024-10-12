package com.marshals.restcontroller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {
	//Only 1 method - Display Portfolio
	//For display portfolio - must return holdings with current price - Must query FMTS for it
	//Call portfolio service - get holdings; Call fmts dao get live prices; 
	//Update avg price of holdings based on fmts dao
	
	//No Controller method for Update client portfolio ; Done as a part of executeTrade
}
