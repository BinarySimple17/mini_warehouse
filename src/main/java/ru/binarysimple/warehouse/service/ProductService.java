package ru.binarysimple.warehouse.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.binarysimple.warehouse.dto.ProductDto;
import ru.binarysimple.warehouse.filter.ProductFilter;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    Page<ProductDto> getAll(ProductFilter filter, Pageable pageable);

    ProductDto getOne(Long id);

    List<ProductDto> getMany(List<Long> ids);

    ProductDto create(ProductDto dto);

    ProductDto patch(Long id, JsonNode patchNode) throws IOException;

    List<Long> patchMany(List<Long> ids, JsonNode patchNode) throws IOException;

    ProductDto delete(Long id);

    void deleteMany(List<Long> ids);
}
