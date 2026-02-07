package ru.binarysimple.warehouse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.binarysimple.warehouse.dto.CatalogFullDto;
import ru.binarysimple.warehouse.dto.OrderPositionDto;
import ru.binarysimple.warehouse.dto.OrderResultDto;
import ru.binarysimple.warehouse.filter.CatalogFilter;
import ru.binarysimple.warehouse.kafka.WarehouseReservationResponseEvent;
import ru.binarysimple.warehouse.mapper.CatalogMapper;
import ru.binarysimple.warehouse.model.Catalog;
import ru.binarysimple.warehouse.repository.CatalogRepository;

import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class CatalogServiceImpl implements CatalogService {

    private final CatalogMapper catalogMapper;

    private final CatalogRepository catalogRepository;

    private final ObjectMapper objectMapper;

    @Override
    public Page<CatalogFullDto> getAll(CatalogFilter filter, Pageable pageable) {
        Specification<Catalog> spec = filter.toSpecification();
        Page<Catalog> catalogs = catalogRepository.findAll(spec, pageable);
        return catalogs.map(catalogMapper::toCatalogFullDto);
    }

    @Override
    public CatalogFullDto getOne(Long id) {
        Optional<Catalog> catalogOptional = catalogRepository.findById(id);
        return catalogMapper.toCatalogFullDto(catalogOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id))));
    }

//    @Override
//    public List<CatalogFullDto> getMany(List<Long> ids) {
//        List<Catalog> catalogs = catalogRepository.findAllById(ids);
//        return catalogs.stream()
//                .map(catalogMapper::toCatalogFullDto)
//                .toList();
//    }

    @Override
    public CatalogFullDto create(CatalogFullDto dto) {
        Catalog catalog = catalogMapper.toEntity(dto);
        Catalog resultCatalog = catalogRepository.save(catalog);
        return catalogMapper.toCatalogFullDto(resultCatalog);
    }

//    @Override
//    public CatalogFullDto patch(Long id, JsonNode patchNode) throws IOException {
//        Catalog catalog = catalogRepository.findById(id).orElseThrow(() ->
//                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
//
//        CatalogFullDto catalogFullDto = catalogMapper.toCatalogFullDto(catalog);
//        objectMapper.readerForUpdating(catalogFullDto).readValue(patchNode);
//        catalogMapper.updateWithNull(catalogFullDto, catalog);
//
//        Catalog resultCatalog = catalogRepository.save(catalog);
//        return catalogMapper.toCatalogFullDto(resultCatalog);
//    }

//    @Override
//    public List<Long> patchMany(List<Long> ids, JsonNode patchNode) throws IOException {
//        Collection<Catalog> catalogs = catalogRepository.findAllById(ids);
//
//        for (Catalog catalog : catalogs) {
//            CatalogFullDto catalogFullDto = catalogMapper.toCatalogFullDto(catalog);
//            objectMapper.readerForUpdating(catalogFullDto).readValue(patchNode);
//            catalogMapper.updateWithNull(catalogFullDto, catalog);
//        }
//
//        List<Catalog> resultCatalogs = catalogRepository.saveAll(catalogs);
//        return resultCatalogs.stream()
//                .map(Catalog::getId)
//                .toList();
//    }

    @Override
    public CatalogFullDto delete(Long id) {
        Catalog catalog = catalogRepository.findById(id).orElse(null);
        if (catalog != null) {
            catalogRepository.delete(catalog);
        }
        return catalogMapper.toCatalogFullDto(catalog);
    }

    @Override
    public void deleteMany(List<Long> ids) {
        catalogRepository.deleteAllById(ids);
    }


    @Override
    public WarehouseReservationResponseEvent reserveOrder(OrderResultDto order, UUID sagaId) {

        List<Catalog> catalogsResult = new ArrayList<>();

        WarehouseReservationResponseEvent responseEvent = WarehouseReservationResponseEvent.builder()
                .sagaId(sagaId)
                .success(false)
                .order(order)
                .build();

        try {

            Map<Long, OrderPositionDto> orderPositions = order.getOrderPositions().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            OrderPositionDto::getProductId,
                            position -> position
                    ));

            List<Long> productIds = new java.util.ArrayList<>(orderPositions.keySet());

            List<Catalog> catalogsSource = catalogRepository.findByShopIdAndProductIdIn(order.getShopId(), productIds);


            for (Catalog catalog : catalogsSource) {
                OrderPositionDto position = orderPositions.get(catalog.getProduct().getId());
                if (position != null && catalog.getQuantity() >= position.getQuantity()) {
                    catalog.setQuantity(catalog.getQuantity() - position.getQuantity());
                    catalogsResult.add(catalog);
                }
            }

            //если все позиции нашлись и уменьшились, то сохраняем и считаем, что успех
            if (catalogsResult.size() == order.getOrderPositions().size()) {
                catalogRepository.saveAll(catalogsResult);
                responseEvent.setSuccess(true);
                log.info("Request processed eventId: {}", responseEvent.getEventId());
            } else {
                responseEvent.setMessage("Request NOT processed [not all positions reserved]");
                log.info("Request NOT processed [not all positions reserved] eventId: {}", responseEvent.getEventId());
            }
        } catch (Exception e) {
            log.error("Error reserveOrder: {}", e.getMessage());
            responseEvent.setMessage(e.getMessage());
        }

        return responseEvent;
    }
}
