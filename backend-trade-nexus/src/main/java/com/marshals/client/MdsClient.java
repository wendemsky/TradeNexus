package com.marshals.client;

import com.marshals.dto.MarketStatus;
import com.marshals.dto.Price;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class MdsClient {

    @Value("${mds.url:http://localhost:3001}")
    private String mdsUrl;

    private final RestTemplate restTemplate;

    public MdsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Price> getAllPrices() {
        Price[] prices = restTemplate.getForObject(mdsUrl + "/prices", Price[].class);
        return prices != null ? Arrays.asList(prices) : Collections.emptyList();
    }

    public Price getPrice(String instrumentId) {
        return restTemplate.getForObject(mdsUrl + "/prices/" + instrumentId, Price.class);
    }

    public List<Price> getPriceHistory(String instrumentId) {
        Price[] prices = restTemplate.getForObject(
                mdsUrl + "/prices/" + instrumentId + "/history", Price[].class);
        return prices != null ? Arrays.asList(prices) : Collections.emptyList();
    }

    public MarketStatus getMarketStatus() {
        return restTemplate.getForObject(mdsUrl + "/market-status", MarketStatus.class);
    }
}
