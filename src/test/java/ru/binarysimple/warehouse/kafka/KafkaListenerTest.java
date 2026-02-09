package ru.binarysimple.warehouse.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import ru.binarysimple.warehouse.dto.OrderPositionDto;
import ru.binarysimple.warehouse.dto.OrderResultDto;
import ru.binarysimple.warehouse.model.EventType;
import ru.binarysimple.warehouse.model.ParentType;
import ru.binarysimple.warehouse.service.CatalogServiceImpl;
import ru.binarysimple.warehouse.service.OutboxService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KafkaListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CatalogServiceImpl catalogService;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private KafkaListener kafkaListener;

    @Captor
    private ArgumentCaptor<EventType> eventTypeCaptor;

    @Captor
    private ArgumentCaptor<String> parentIdCaptor;

    @Captor
    private ArgumentCaptor<ParentType> parentTypeCaptor;

    @Captor
    private ArgumentCaptor<Object> payloadCaptor;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    private UUID sagaId;
    private OrderResultDto order;
    private WarehouseReservationRequestEvent reservationRequest;
    private WarehouseCompensationRequestEvent compensationRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        sagaId = UUID.randomUUID();
        order = OrderResultDto.builder()
                .id(1L)
                .username("testUser")
                .orderPositions(List.of(
                    OrderPositionDto.builder()
                            .productId(1L)
                            .quantity(2)
                            .price(BigDecimal.valueOf(100))
                            .build()
                ))
                .totalCost(BigDecimal.valueOf(200))
                .createdAt(LocalDateTime.now())
                .shopId(1L)
                .build();
                
        reservationRequest = WarehouseReservationRequestEvent.builder()
                .eventId(UUID.randomUUID())
                .sagaId(sagaId)
                .order(order)
                .timestamp(LocalDateTime.now())
                .build();
                
        compensationRequest = WarehouseCompensationRequestEvent.builder()
                .eventId(UUID.randomUUID())
                .sagaId(sagaId)
                .order(order)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void handleWarehouseRequest_SuccessfulReservation() throws Exception {
        // Given
        String message = "test message";
        WarehouseReservationResponseEvent successResponse = WarehouseReservationResponseEvent.builder()
                .sagaId(sagaId)
                .success(true)
                .order(order)
                .build();
        
        when(objectMapper.readValue(message, WarehouseReservationRequestEvent.class))
                .thenReturn(reservationRequest);
        when(catalogService.reserveOrder(order, sagaId)).thenReturn(successResponse);

        // When
        kafkaListener.handleWarehouseRequest(message);

        // Then
        verify(outboxService).saveEvent(
                eventTypeCaptor.capture(),
                parentIdCaptor.capture(),
                parentTypeCaptor.capture(),
                payloadCaptor.capture(),
                topicCaptor.capture()
        );
        
        assertEquals(EventType.WAREHOUSE_RESERVED, eventTypeCaptor.getValue());
        assertEquals(sagaId.toString(), parentIdCaptor.getValue());
        assertEquals(ParentType.SAGA, parentTypeCaptor.getValue());
        assertEquals("warehouse.reserve.response", topicCaptor.getValue());
        
        Object capturedPayload = payloadCaptor.getValue();
        assertTrue(capturedPayload instanceof WarehouseReservationResponseEvent);
        WarehouseReservationResponseEvent capturedEvent = (WarehouseReservationResponseEvent) capturedPayload;
        assertTrue(capturedEvent.getSuccess());
        assertEquals(sagaId, capturedEvent.getSagaId());
        assertEquals(order, capturedEvent.getOrder());
    }

    @Test
    void handleWarehouseRequest_FailedReservation() throws Exception {
        // Given
        String message = "test message";
        WarehouseReservationResponseEvent failureResponse = WarehouseReservationResponseEvent.builder()
                .sagaId(sagaId)
                .success(false)
                .message("Not enough stock")
                .order(order)
                .build();
        
        when(objectMapper.readValue(message, WarehouseReservationRequestEvent.class))
                .thenReturn(reservationRequest);
        when(catalogService.reserveOrder(order, sagaId)).thenReturn(failureResponse);

        // When
        kafkaListener.handleWarehouseRequest(message);

        // Then
        verify(outboxService).saveEvent(
                eventTypeCaptor.capture(),
                parentIdCaptor.capture(),
                parentTypeCaptor.capture(),
                payloadCaptor.capture(),
                topicCaptor.capture()
        );
        
        assertEquals(EventType.WAREHOUSE_RESERVATION_FAILED, eventTypeCaptor.getValue());
        assertEquals(sagaId.toString(), parentIdCaptor.getValue());
        assertEquals(ParentType.SAGA, parentTypeCaptor.getValue());
        assertEquals("warehouse.reserve.response", topicCaptor.getValue());
        
        Object capturedPayload = payloadCaptor.getValue();
        assertTrue(capturedPayload instanceof WarehouseReservationResponseEvent);
        WarehouseReservationResponseEvent capturedEvent = (WarehouseReservationResponseEvent) capturedPayload;
        assertFalse(capturedEvent.getSuccess());
        assertEquals(sagaId, capturedEvent.getSagaId());
        assertEquals(order, capturedEvent.getOrder());
        assertEquals("Not enough stock", capturedEvent.getMessage());
    }

    @Test
    void handleWarehouseRequest_JsonParsingError() throws Exception {
        // Given
        String message = "invalid json";
        
        when(objectMapper.readValue(message, WarehouseReservationRequestEvent.class))
                .thenThrow(new RuntimeException("JSON parse error"));

        // When
        kafkaListener.handleWarehouseRequest(message);

        // Then
        verify(outboxService, never()).saveEvent(any(), any(), any(), any(), any());
    }

    @Test
    void handleWarehouseCompensateRequest_SuccessfulCompensation() throws Exception {
        // Given
        String message = "test message";
        WarehouseCompensationResponseEvent successResponse = WarehouseCompensationResponseEvent.builder()
                .sagaId(sagaId)
                .success(true)
                .order(order)
                .build();
        
        when(objectMapper.readValue(message, WarehouseCompensationRequestEvent.class))
                .thenReturn(compensationRequest);
        when(catalogService.compensateOrder(order, sagaId)).thenReturn(successResponse);

        // When
        kafkaListener.handleWarehouseCompensateRequest(message);

        // Then
        verify(outboxService).saveEvent(
                eventTypeCaptor.capture(),
                parentIdCaptor.capture(),
                parentTypeCaptor.capture(),
                payloadCaptor.capture(),
                topicCaptor.capture()
        );
        
        assertEquals(EventType.WAREHOUSE_COMPENSATION_COMPLETED, eventTypeCaptor.getValue());
        assertEquals(sagaId.toString(), parentIdCaptor.getValue());
        assertEquals(ParentType.SAGA, parentTypeCaptor.getValue());
        assertEquals("warehouse.compensate.response", topicCaptor.getValue());
        
        Object capturedPayload = payloadCaptor.getValue();
        assertTrue(capturedPayload instanceof WarehouseCompensationResponseEvent);
        WarehouseCompensationResponseEvent capturedEvent = (WarehouseCompensationResponseEvent) capturedPayload;
        assertTrue(capturedEvent.getSuccess());
        assertEquals(sagaId, capturedEvent.getSagaId());
        assertEquals(order, capturedEvent.getOrder());
    }

    @Test
    void handleWarehouseCompensateRequest_FailedCompensation() throws Exception {
        // Given
        String message = "test message";
        WarehouseCompensationResponseEvent failureResponse = WarehouseCompensationResponseEvent.builder()
                .sagaId(sagaId)
                .success(false)
                .message("Compensation failed")
                .order(order)
                .build();
        
        when(objectMapper.readValue(message, WarehouseCompensationRequestEvent.class))
                .thenReturn(compensationRequest);
        when(catalogService.compensateOrder(order, sagaId)).thenReturn(failureResponse);

        // When
        kafkaListener.handleWarehouseCompensateRequest(message);

        // Then
        verify(outboxService).saveEvent(
                eventTypeCaptor.capture(),
                parentIdCaptor.capture(),
                parentTypeCaptor.capture(),
                payloadCaptor.capture(),
                topicCaptor.capture()
        );
        
        assertEquals(EventType.WAREHOUSE_COMPENSATION_FAILED, eventTypeCaptor.getValue());
        assertEquals(sagaId.toString(), parentIdCaptor.getValue());
        assertEquals(ParentType.SAGA, parentTypeCaptor.getValue());
        assertEquals("warehouse.compensate.response", topicCaptor.getValue());
        
        Object capturedPayload = payloadCaptor.getValue();
        assertTrue(capturedPayload instanceof WarehouseCompensationResponseEvent);
        WarehouseCompensationResponseEvent capturedEvent = (WarehouseCompensationResponseEvent) capturedPayload;
        assertFalse(capturedEvent.getSuccess());
        assertEquals(sagaId, capturedEvent.getSagaId());
        assertEquals(order, capturedEvent.getOrder());
        assertEquals("Compensation failed", capturedEvent.getMessage());
    }

    @Test
    void handleWarehouseCompensateRequest_JsonParsingError() throws Exception {
        // Given
        String message = "invalid json";
        
        when(objectMapper.readValue(message, WarehouseCompensationRequestEvent.class))
                .thenThrow(new RuntimeException("JSON parse error"));

        // When
        kafkaListener.handleWarehouseCompensateRequest(message);

        // Then
        verify(outboxService, never()).saveEvent(any(), any(), any(), any(), any());
    }
}