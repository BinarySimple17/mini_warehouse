package ru.binarysimple.warehouse.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.binarysimple.warehouse.dto.CatalogFullDto;
import ru.binarysimple.warehouse.dto.OrderResultDto;
import ru.binarysimple.warehouse.filter.CatalogFilter;
import ru.binarysimple.warehouse.kafka.WarehouseReservationResponseEvent;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface CatalogService {
    Page<CatalogFullDto> getAll(CatalogFilter filter, Pageable pageable);

    CatalogFullDto getOne(Long id);

//    List<CatalogFullDto> getMany(List<Long> ids);

    CatalogFullDto create(CatalogFullDto dto);

//    CatalogFullDto patch(Long id, JsonNode patchNode) throws IOException;

//    List<Long> patchMany(List<Long> ids, JsonNode patchNode) throws IOException;

    CatalogFullDto delete(Long id);

    void deleteMany(List<Long> ids);

    WarehouseReservationResponseEvent reserveOrder(OrderResultDto order, UUID sagaId);
}
