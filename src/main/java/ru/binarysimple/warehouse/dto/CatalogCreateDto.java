package ru.binarysimple.warehouse.dto;

import lombok.Value;
import ru.binarysimple.warehouse.model.Catalog;

import java.math.BigDecimal;

/**
 * DTO for {@link Catalog}
 */
@Value
public class CatalogCreateDto {
    Long shopId;
    BigDecimal price;
    Integer quantity;
    Integer reservedQuantity;
    Boolean active;
}