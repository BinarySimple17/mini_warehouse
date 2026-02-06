package ru.binarysimple.warehouse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.binarysimple.warehouse.dto.CatalogFullDto;
import ru.binarysimple.warehouse.mapper.CatalogMapper;
import ru.binarysimple.warehouse.model.Catalog;
import ru.binarysimple.warehouse.repository.CatalogRepository;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CatalogServiceImpl implements CatalogService {

    private final CatalogMapper catalogMapper;

    private final CatalogRepository catalogRepository;

    private final ObjectMapper objectMapper;

    @Override
    public Page<CatalogFullDto> getAll(Pageable pageable) {
        Page<Catalog> catalogs = catalogRepository.findAll(pageable);
        return catalogs.map(catalogMapper::toCatalogDto3);
    }

    @Override
    public CatalogFullDto getOne(Long id) {
        Optional<Catalog> catalogOptional = catalogRepository.findById(id);
        return catalogMapper.toCatalogDto3(catalogOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id))));
    }

    @Override
    public List<CatalogFullDto> getMany(List<Long> ids) {
        List<Catalog> catalogs = catalogRepository.findAllById(ids);
        return catalogs.stream()
                .map(catalogMapper::toCatalogDto3)
                .toList();
    }

    @Override
    public CatalogFullDto create(CatalogFullDto dto) {
        Catalog catalog = catalogMapper.toEntity(dto);
        Catalog resultCatalog = catalogRepository.save(catalog);
        return catalogMapper.toCatalogDto3(resultCatalog);
    }

    @Override
    public CatalogFullDto patch(Long id, JsonNode patchNode) throws IOException {
        Catalog catalog = catalogRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        CatalogFullDto catalogFullDto = catalogMapper.toCatalogDto3(catalog);
        objectMapper.readerForUpdating(catalogFullDto).readValue(patchNode);
        catalogMapper.updateWithNull(catalogFullDto, catalog);

        Catalog resultCatalog = catalogRepository.save(catalog);
        return catalogMapper.toCatalogDto3(resultCatalog);
    }

    @Override
    public List<Long> patchMany(List<Long> ids, JsonNode patchNode) throws IOException {
        Collection<Catalog> catalogs = catalogRepository.findAllById(ids);

        for (Catalog catalog : catalogs) {
            CatalogFullDto catalogFullDto = catalogMapper.toCatalogDto3(catalog);
            objectMapper.readerForUpdating(catalogFullDto).readValue(patchNode);
            catalogMapper.updateWithNull(catalogFullDto, catalog);
        }

        List<Catalog> resultCatalogs = catalogRepository.saveAll(catalogs);
        return resultCatalogs.stream()
                .map(Catalog::getId)
                .toList();
    }

    @Override
    public CatalogFullDto delete(Long id) {
        Catalog catalog = catalogRepository.findById(id).orElse(null);
        if (catalog != null) {
            catalogRepository.delete(catalog);
        }
        return catalogMapper.toCatalogDto3(catalog);
    }

    @Override
    public void deleteMany(List<Long> ids) {
        catalogRepository.deleteAllById(ids);
    }
}
