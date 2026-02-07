package ru.binarysimple.warehouse.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import ru.binarysimple.warehouse.dto.ErrorDto;

@Getter
public class OutboxServiceException extends RuntimeException {
    private final ErrorDto errorDto;

    public OutboxServiceException(String message) {
        super(message);
        this.errorDto = new ErrorDto(HttpStatus.BAD_REQUEST.value(), message);
    }

    public OutboxServiceException(String message, ErrorDto errorDto) {
        super(message);
        this.errorDto = errorDto;
    }

}
