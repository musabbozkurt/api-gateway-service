package com.mb.stockexchangeservice.api.controller;

import com.mb.stockexchangeservice.api.request.ApiStockExchangeRequest;
import com.mb.stockexchangeservice.api.request.ApiStockRequest;
import com.mb.stockexchangeservice.api.response.ApiStockExchangeResponse;
import com.mb.stockexchangeservice.api.response.ApiStockResponse;
import com.mb.stockexchangeservice.mapper.StockExchangeMapper;
import com.mb.stockexchangeservice.mapper.StockMapper;
import com.mb.stockexchangeservice.service.StockExchangeService;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stock-exchange")
public class StockExchangeController {

    private final StockExchangeService stockExchangeService;
    private final StockMapper stockMapper;
    private final StockExchangeMapper stockExchangeMapper;

    @PostMapping("/")
    @Observed(name = "StockExchange")
    @PreAuthorize("hasRole('CREATE_STOCK_EXCHANGE')")
    @Operation(description = "Create stock exchange.")
    public ResponseEntity<ApiStockExchangeResponse> createStockExchange(@RequestBody ApiStockExchangeRequest apiStockExchangeRequest) {
        log.info("Received a request to create stock. createStockExchange - apiStockExchangeRequest: {}", apiStockExchangeRequest);
        return new ResponseEntity<>(stockExchangeMapper.map(stockExchangeService.createStockExchange(stockExchangeMapper.map(apiStockExchangeRequest))), HttpStatus.CREATED);
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasRole('GET_STOCK')")
    @Observed(name = "getStocksByStockExchange")
    @Operation(description = "List stocks of a specific stock exchange.")
    public ResponseEntity<List<ApiStockResponse>> getStocksByStockExchangeName(@PathVariable String name) {
        log.info("Received a request to get stocks by stock exchange name. getStocksByStockExchangeName - name: {}", name);
        return ResponseEntity.ok(stockMapper.map(stockExchangeService.findByName(name).getStocks()));
    }

    @PostMapping("/{name}")
    @PreAuthorize("hasRole('ADD_STOCK')")
    @Observed(name = "addStockToStockExchange")
    @Operation(description = "Add a stock to a stock exchange.")
    public ResponseEntity<ApiStockExchangeResponse> addStockToStockExchange(@PathVariable String name, @RequestBody ApiStockRequest stock) {
        log.info("Received a request to add stock to stock exchange. addStockToStockExchange - name: {}, ApiStockRequest: {}", name, stock);
        return ResponseEntity.ok(stockExchangeMapper.map(stockExchangeService.addStockToStockExchange(name, stockMapper.map(stock))));
    }

    @DeleteMapping("/{name}/{stockName}")
    @PreAuthorize("hasRole('REMOVE_STOCK')")
    @Observed(name = "deleteStockFromStockExchange")
    @Operation(description = "Delete a stock from a stock exchange.")
    public ResponseEntity<ApiStockExchangeResponse> deleteStockFromStockExchange(@PathVariable String name, @PathVariable String stockName) {
        log.info("Received a request to delete stock from stock exchange. deleteStockFromStockExchange - name: {}, ApiStockRequest: {}", name, stockName);
        return ResponseEntity.ok(stockExchangeMapper.map(stockExchangeService.deleteStockFromStockExchange(name, stockName)));
    }
}
