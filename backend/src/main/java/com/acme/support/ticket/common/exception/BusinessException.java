package com.acme.support.ticket.common.exception;

/**
 * 业务异常，用于承载可预期的领域校验错误。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
