package ru.binarysimple.warehouse.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.binarysimple.warehouse.dto.ProductDto;
import ru.binarysimple.warehouse.model.Product;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {
    Product toEntity(ProductDto productDto);

    ProductDto toProductDto(Product product);

    Product updateWithNull(ProductDto productDto, @MappingTarget Product product);
}