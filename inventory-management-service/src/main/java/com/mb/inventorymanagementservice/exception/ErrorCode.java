package com.mb.inventorymanagementservice.exception;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.http.HttpStatus;

@JsonDeserialize(as = InventoryManagementServiceErrorCode.class)
public interface ErrorCode {

    HttpStatus getHttpStatus();

    String getCode();
}
