package ru.binarysimple.warehouse.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.binarysimple.warehouse.dto.CatalogFullDto;
import ru.binarysimple.warehouse.model.Catalog;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {ProductMapper.class})
public interface CatalogMapper {
    Catalog toEntity(CatalogFullDto catalogFullDto);

    CatalogFullDto toCatalogFullDto(Catalog catalog);

    Catalog updateWithNull(CatalogFullDto catalogFullDto, @MappingTarget Catalog catalog);
}