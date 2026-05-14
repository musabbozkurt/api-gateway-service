package com.mb.inventorymanagementservice.exception;

public class InvalidParameterException extends BaseException {

    public InvalidParameterException() {
        super(InventoryManagementServiceErrorCode.INVALID_VALUE);
    }
}
