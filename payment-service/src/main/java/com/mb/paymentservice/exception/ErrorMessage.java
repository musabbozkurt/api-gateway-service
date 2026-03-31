package com.mb.paymentservice.exception;

import java.util.List;

public record ErrorMessage(String errorCode, List<Argument> arguments, String defaultMessage) {

}
