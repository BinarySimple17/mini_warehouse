package ru.binarysimple.warehouse.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link ru.binarysimple.warehouse.model.Product}
 */
@Value
public class ProductDto {
    Long id;
    @NotNull
    @NotEmpty
    String name;
    String description;
    @NotNull
    @NotEmpty
    String sku;
}