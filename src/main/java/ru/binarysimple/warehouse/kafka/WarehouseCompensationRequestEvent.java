package ru.binarysimple.warehouse.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.binarysimple.warehouse.dto.OrderResultDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WarehouseCompensationRequestEvent {

    private UUID eventId;
    private UUID sagaId;
    private OrderResultDto order;

    private LocalDateTime timestamp;
}
