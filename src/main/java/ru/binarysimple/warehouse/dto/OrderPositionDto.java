package ru.binarysimple.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderPositionDto {
    @NotNull
    Long productId;
    @NotNull
    BigDecimal price;
    @NotNull
    Integer quantity;

}