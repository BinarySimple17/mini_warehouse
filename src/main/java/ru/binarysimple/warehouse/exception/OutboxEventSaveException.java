package ru.binarysimple.warehouse.exception;

import lombok.Getter;

@Getter
public class OutboxEventSaveException extends RuntimeException {

    public OutboxEventSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
