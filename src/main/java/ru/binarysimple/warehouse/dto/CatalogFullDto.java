package ru.binarysimple.warehouse.dto;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for {@link ru.binarysimple.warehouse.model.Catalog}
 */
@Value
public class CatalogFullDto {
    Long id;
    Long shopId;
    BigDecimal price;
    Integer quantity;
    Integer reservedQuantity;
    Boolean active;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    ProductDto product;
}