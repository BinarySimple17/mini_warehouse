package ru.binarysimple.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import ru.binarysimple.warehouse.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResultDto {
    Long id;
    String username;
    List<OrderPositionDto> orderPositions;
    BigDecimal totalCost;
    LocalDateTime createdAt;
    Long shopId;
    OrderStatus status;
    Long deliveryId;
}