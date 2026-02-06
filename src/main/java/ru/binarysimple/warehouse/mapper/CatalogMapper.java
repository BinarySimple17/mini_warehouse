package ru.binarysimple.warehouse.mapper;

import org.mapstruct.*;
import ru.binarysimple.warehouse.dto.CatalogFullDto;
import ru.binarysimple.warehouse.model.Catalog;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {ProductMapper.class})
public interface CatalogMapper {
    Catalog toEntity(CatalogFullDto catalogFullDto);

    @AfterMapping
    default void linkProducts(@MappingTarget Catalog catalog) {
        catalog.getProducts().forEach(product -> product.setCatalog(catalog));
    }

    CatalogFullDto toCatalogDto3(Catalog catalog);

    Catalog updateWithNull(CatalogFullDto catalogFullDto, @MappingTarget Catalog catalog);
}