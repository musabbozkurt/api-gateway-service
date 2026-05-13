package com.mb.stockexchangeservice.api.controller;

import com.mb.stockexchangeservice.api.request.ApiStockRequest;
import com.mb.stockexchangeservice.api.response.ApiStockResponse;
import com.mb.stockexchangeservice.mapper.StockMapper;
import com.mb.stockexchangeservice.service.StockService;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stock")
public class StockController {

    private final StockService stockService;
    private final StockMapper stockMapper;

    @GetMapping("/")
    @Observed(name = "getAllStocks")
    @PreAuthorize("hasRole('GET_STOCK')")
    @Operation(description = "Get all stock.")
    public ResponseEntity<Page<ApiStockResponse>> getAllStocks(Pageable pageable) {
        log.info("Received a request to get all stocks. getAllStocks.");
        return new ResponseEntity<>(stockMapper.map(stockService.getAllStocks(pageable)), HttpStatus.OK);
    }

    @GetMapping("/{name}")
    @Observed(name = "getStockByName")
    @PreAuthorize("hasRole('GET_STOCK')")
    @Operation(description = "Get stock by name.")
    public ResponseEntity<ApiStockResponse> getStockByName(@PathVariable String name) {
        log.info("Received a request to get stock by name. getStockByName - name: {}", name);
        return new ResponseEntity<>(stockMapper.map(stockService.getStockByName(name)), HttpStatus.OK);
    }

    @PostMapping("/")
    @Observed(name = "createStock")
    @PreAuthorize("hasRole('CREATE_STOCK')")
    @Operation(description = "Create stock.")
    public ResponseEntity<ApiStockResponse> createStock(@RequestBody ApiStockRequest apiStockRequest) {
        log.info("Received a request to create stock. createStock - apiStockRequest: {}", apiStockRequest);
        return new ResponseEntity<>(stockMapper.map(stockService.createStock(stockMapper.map(apiStockRequest))), HttpStatus.CREATED);
    }

    @PutMapping("/{stockId}")
    @Observed(name = "updateStockById")
    @PreAuthorize("hasRole('UPDATE_STOCK')")
    @Operation(description = "Update stock by id.")
    public ResponseEntity<ApiStockResponse> updateStockById(@PathVariable Long stockId, @RequestBody ApiStockRequest apiStockRequest) {
        log.info("Received a request to update stock by id. updateStockById - stock: {}, apiStockRequest: {}", stockId, apiStockRequest);
        return new ResponseEntity<>(stockMapper.map(stockService.updateStockById(stockId, stockMapper.map(apiStockRequest))), HttpStatus.OK);
    }

    @DeleteMapping("/{stockId}")
    @Observed(name = "deleteStockById")
    @PreAuthorize("hasRole('DELETE_STOCK')")
    @Operation(description = "Delete stock by id")
    public ResponseEntity<String> deleteStockById(@PathVariable Long stockId) {
        log.info("Received a request to delete stock by id. deleteStockById - stockId: {}", stockId);
        stockService.deleteStockById(stockId);
        return new ResponseEntity<>("Stock deleted successfully.", HttpStatus.OK);
    }
}
