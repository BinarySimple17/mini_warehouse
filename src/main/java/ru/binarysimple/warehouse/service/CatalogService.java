package ru.binarysimple.warehouse.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.binarysimple.warehouse.dto.CatalogFullDto;

import java.io.IOException;
import java.util.List;

public interface CatalogService {
    Page<CatalogFullDto> getAll(Pageable pageable);

    CatalogFullDto getOne(Long id);

    List<CatalogFullDto> getMany(List<Long> ids);

    CatalogFullDto create(CatalogFullDto dto);

    CatalogFullDto patch(Long id, JsonNode patchNode) throws IOException;

    List<Long> patchMany(List<Long> ids, JsonNode patchNode) throws IOException;

    CatalogFullDto delete(Long id);

    void deleteMany(List<Long> ids);
}
