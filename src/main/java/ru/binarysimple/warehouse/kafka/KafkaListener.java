package ru.binarysimple.warehouse.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.binarysimple.warehouse.model.EventType;
import ru.binarysimple.warehouse.model.ParentType;
import ru.binarysimple.warehouse.service.CatalogServiceImpl;
import ru.binarysimple.warehouse.service.OutboxService;

@RequiredArgsConstructor
@Slf4j
@Component
public class KafkaListener {

    private final ObjectMapper objectMapper;

    private final CatalogServiceImpl catalogService;

    private final OutboxService outboxService;

    @org.springframework.kafka.annotation.KafkaListener(
            id = "warehouseListener",
            topics = "warehouse.reserve.request")
    @Transactional
    public void handleWarehouseRequest(String message) {

        log.debug("handleWarehouseRequest from kafka {}", message);

        try {
            WarehouseReservationRequestEvent event = objectMapper.readValue(message, WarehouseReservationRequestEvent.class);

            WarehouseReservationResponseEvent responseEvent = catalogService.reserveOrder(event.getOrder(), event.getSagaId());
            outboxService.saveEvent(
                    responseEvent.getSuccess()?EventType.WAREHOUSE_RESERVED:EventType.WAREHOUSE_RESERVATION_FAILED,
                    event.getSagaId().toString(),
                    ParentType.SAGA,
                    responseEvent,
                    "warehouse.reserve.response"
            );

            log.info("Request processed: {}", message);

        } catch (Exception e) {
            log.error("Failed to process request: {}", WarehouseReservationRequestEvent.class, e);
        }
    }
}
