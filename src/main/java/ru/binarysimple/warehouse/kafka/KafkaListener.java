package ru.binarysimple.warehouse.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.binarysimple.warehouse.model.EventType;
import ru.binarysimple.warehouse.model.ParentType;
import ru.binarysimple.warehouse.model.ProcessedEventId;
import ru.binarysimple.warehouse.repository.ProcessedEventIdRepository;
import ru.binarysimple.warehouse.service.CatalogServiceImpl;
import ru.binarysimple.warehouse.service.OutboxService;

@RequiredArgsConstructor
@Slf4j
@Component
public class KafkaListener {

    private final ObjectMapper objectMapper;

    private final CatalogServiceImpl catalogService;

    private final OutboxService outboxService;

    private final ProcessedEventIdRepository processedEventIdRepository;

    @org.springframework.kafka.annotation.KafkaListener(
            id = "warehouseListenerReserve",
            topics = "warehouse.reserve.request")
    @Transactional
    public void handleWarehouseRequest(String message) {

        log.debug("handleWarehouseRequest from kafka {}", message);

        try {
            WarehouseReservationRequestEvent event = objectMapper.readValue(message, WarehouseReservationRequestEvent.class);

            ProcessedEventId processedEventId = new ProcessedEventId();
            processedEventId.setEventId(event.getEventId().toString());
            processedEventIdRepository.save(processedEventId);

            WarehouseReservationResponseEvent responseEvent = catalogService.reserveOrder(event.getOrder(), event.getSagaId());
            outboxService.saveEvent(
                    responseEvent.getSuccess() ? EventType.WAREHOUSE_RESERVED : EventType.WAREHOUSE_RESERVATION_FAILED,
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

    @org.springframework.kafka.annotation.KafkaListener(
            id = "warehouseListenerCompensate",
            topics = "warehouse.compensate.request")
    @Transactional
    public void handleWarehouseCompensateRequest(String message) {

        log.debug("handleWarehouseCompensateRequest from kafka {}", message);

        try {
            WarehouseCompensationRequestEvent event = objectMapper.readValue(message, WarehouseCompensationRequestEvent.class);

            ProcessedEventId processedEventId = new ProcessedEventId();
            processedEventId.setEventId(event.getEventId().toString());
            processedEventIdRepository.save(processedEventId);

            WarehouseCompensationResponseEvent responseEvent = catalogService.compensateOrder(event.getOrder(), event.getSagaId());
            outboxService.saveEvent(
                    responseEvent.getSuccess() ? EventType.WAREHOUSE_COMPENSATION_COMPLETED : EventType.WAREHOUSE_COMPENSATION_FAILED,
                    event.getSagaId().toString(),
                    ParentType.SAGA,
                    responseEvent,
                    "warehouse.compensate.response"
            );

            log.info("Request compensation processed: {}", message);

        } catch (Exception e) {
            log.error("Failed to process compensation request: {}", WarehouseReservationRequestEvent.class, e);
        }
    }
}
