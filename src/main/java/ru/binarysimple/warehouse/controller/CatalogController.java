package ru.binarysimple.warehouse.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;
import ru.binarysimple.warehouse.dto.CatalogFullDto;
import ru.binarysimple.warehouse.service.CatalogService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/warehouse/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    public PagedModel<CatalogFullDto> getAll(@ParameterObject Pageable pageable) {
        Page<CatalogFullDto> catalogDto3s = catalogService.getAll(pageable);
        return new PagedModel<>(catalogDto3s);
    }

    @GetMapping("/{id}")
    public CatalogFullDto getOne(@PathVariable Long id) {
        return catalogService.getOne(id);
    }

    @GetMapping("/by-ids")
    public List<CatalogFullDto> getMany(@RequestParam List<Long> ids) {
        return catalogService.getMany(ids);
    }

    @PostMapping
    public CatalogFullDto create(@RequestBody CatalogFullDto dto) {
        return catalogService.create(dto);
    }

    @PatchMapping("/{id}")
    public CatalogFullDto patch(@PathVariable Long id, @RequestBody JsonNode patchNode) throws IOException {
        return catalogService.patch(id, patchNode);
    }

    @PatchMapping
    public List<Long> patchMany(@RequestParam List<Long> ids, @RequestBody JsonNode patchNode) throws IOException {
        return catalogService.patchMany(ids, patchNode);
    }

    @DeleteMapping("/{id}")
    public CatalogFullDto delete(@PathVariable Long id) {
        return catalogService.delete(id);
    }

    @DeleteMapping
    public void deleteMany(@RequestParam List<Long> ids) {
        catalogService.deleteMany(ids);
    }
}
