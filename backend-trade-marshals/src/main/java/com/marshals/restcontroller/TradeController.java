package com.marshals.restcontroller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trade")
public class TradeController {
	//Get live instrument prices - from fmts - Directly call fmtsService
	//Display Trade History - call tradeService method
	//Execute trade - call tradeService executeTrade method
		//TradeService method itself takes care of updating portfolio, balance etc no need to do here
	
	//Robo advisor - top 5 buy instruments : Must pass client preferences
	//Robo advisor - top 5 sell instruments : Must pass client preferences
	/*SERVICE method must itself invoke portfolioService and clientService to get curr holdings and balance*/
}
