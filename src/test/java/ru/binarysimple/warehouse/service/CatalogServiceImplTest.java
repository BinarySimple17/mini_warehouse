package ru.binarysimple.warehouse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.binarysimple.warehouse.dto.OrderPositionDto;
import ru.binarysimple.warehouse.dto.OrderResultDto;
import ru.binarysimple.warehouse.kafka.WarehouseCompensationResponseEvent;
import ru.binarysimple.warehouse.kafka.WarehouseReservationResponseEvent;
import ru.binarysimple.warehouse.mapper.CatalogMapper;
import ru.binarysimple.warehouse.model.Catalog;
import ru.binarysimple.warehouse.model.Product;
import ru.binarysimple.warehouse.repository.CatalogRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CatalogServiceImplTest {

    @Mock
    private CatalogRepository catalogRepository;

    @Mock
    private CatalogMapper catalogMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CatalogServiceImpl catalogService;

    private UUID sagaId;
    private OrderResultDto order;
    private Product product1;
    private Product product2;
    private Catalog catalog1;
    private Catalog catalog2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        sagaId = UUID.randomUUID();
        
        // Создаем продукты
        product1 = new Product();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setSku("SKU001");
        
        product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setSku("SKU002");
        
        // Создаем каталоги
        catalog1 = new Catalog();
        catalog1.setId(1L);
        catalog1.setShopId(1L);
        catalog1.setPrice(BigDecimal.valueOf(100));
        catalog1.setQuantity(10);
        catalog1.setProduct(product1);
        
        catalog2 = new Catalog();
        catalog2.setId(2L);
        catalog2.setShopId(1L);
        catalog2.setPrice(BigDecimal.valueOf(200));
        catalog2.setQuantity(5);
        catalog2.setProduct(product2);
        
        // Создаем заказ
        order = OrderResultDto.builder()
                .id(1L)
                .username("testUser")
                .shopId(1L)
                .orderPositions(Arrays.asList(
                    OrderPositionDto.builder()
                            .productId(1L)
                            .quantity(2)
                            .price(BigDecimal.valueOf(100))
                            .build(),
                    OrderPositionDto.builder()
                            .productId(2L)
                            .quantity(3)
                            .price(BigDecimal.valueOf(200))
                            .build()
                ))
                .totalCost(BigDecimal.valueOf(800))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void reserveOrder_SuccessfulReservation_AllProductsAvailable() {
        // Given
        List<Catalog> catalogsSource = Arrays.asList(catalog1, catalog2);
        
        when(catalogRepository.findByShopIdAndProductIdIn(1L, Arrays.asList(1L, 2L)))
                .thenReturn(catalogsSource);
        
        // When
        WarehouseReservationResponseEvent response = catalogService.reserveOrder(order, sagaId);
        
        // Then
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals(sagaId, response.getSagaId());
        assertEquals(order, response.getOrder());
        assertNull(response.getMessage());
        
        // Проверяем, что количество было уменьшено
        assertEquals(8, catalog1.getQuantity()); // 10 - 2 = 8
        assertEquals(2, catalog2.getQuantity()); // 5 - 3 = 2
        
        // Проверяем, что репозиторий был вызван для сохранения
        verify(catalogRepository).saveAll(anyList());
    }

    @Test
    void reserveOrder_FailedReservation_NotEnoughStock() {
        // Given
        // Уменьшаем количество товара, чтобы его не хватило на заказ
        catalog1.setQuantity(1); // Нужно 2, но есть только 1
        
        List<Catalog> catalogsSource = Arrays.asList(catalog1, catalog2);
        
        when(catalogRepository.findByShopIdAndProductIdIn(1L, Arrays.asList(1L, 2L)))
                .thenReturn(catalogsSource);
        
        // When
        WarehouseReservationResponseEvent response = catalogService.reserveOrder(order, sagaId);
        
        // Then
        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals(sagaId, response.getSagaId());
        assertEquals(order, response.getOrder());
        assertEquals("Request NOT processed [not all positions reserved]", response.getMessage());
        
        // Проверяем, что количество товара 1 не изменилось (так как резервация не прошла)
        assertEquals(1, catalog1.getQuantity());
        assertEquals(5, catalog2.getQuantity());
        
        // Проверяем, что репозиторий НЕ был вызван для сохранения
        verify(catalogRepository, never()).saveAll(anyList());
    }

    @Test
    void reserveOrder_FailedReservation_MissingProduct() {
        // Given
        // В репозитории только один товар, хотя в заказе два
        List<Catalog> catalogsSource = Collections.singletonList(catalog1);
        
        when(catalogRepository.findByShopIdAndProductIdIn(1L, Arrays.asList(1L, 2L)))
                .thenReturn(catalogsSource);
        
        // When
        WarehouseReservationResponseEvent response = catalogService.reserveOrder(order, sagaId);
        
        // Then
        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals(sagaId, response.getSagaId());
        assertEquals(order, response.getOrder());
        assertEquals("Request NOT processed [not all positions reserved]", response.getMessage());
        
        // Проверяем, что количество товара не изменилось
        assertEquals(10, catalog1.getQuantity());
        
        // Проверяем, что репозиторий НЕ был вызван для сохранения
        verify(catalogRepository, never()).saveAll(anyList());
    }

    @Test
    void reserveOrder_ExceptionDuringProcessing() {
        // Given
        when(catalogRepository.findByShopIdAndProductIdIn(1L, Arrays.asList(1L, 2L)))
                .thenThrow(new RuntimeException("Database error"));
        
        // When
        WarehouseReservationResponseEvent response = catalogService.reserveOrder(order, sagaId);
        
        // Then
        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals(sagaId, response.getSagaId());
        assertEquals(order, response.getOrder());
        assertEquals("Database error", response.getMessage());
        
        // Проверяем, что репозиторий НЕ был вызван для сохранения
        verify(catalogRepository, never()).saveAll(anyList());
    }

    @Test
    void compensateOrder_SuccessfulCompensation_AllProductsAvailable() {
        // Given
        // Устанавливаем количество после резервации
        catalog1.setQuantity(8); // Было 10, зарезервировано 2
        catalog2.setQuantity(2); // Было 5, зарезервировано 3
        
        List<Catalog> catalogsSource = Arrays.asList(catalog1, catalog2);
        
        when(catalogRepository.findByShopIdAndProductIdIn(1L, Arrays.asList(1L, 2L)))
                .thenReturn(catalogsSource);
        
        // When
        WarehouseCompensationResponseEvent response = catalogService.compensateOrder(order, sagaId);
        
        // Then
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals(sagaId, response.getSagaId());
        assertEquals(order, response.getOrder());
        assertNull(response.getMessage());
        
        // Проверяем, что количество было увеличено (компенсировано)
        assertEquals(10, catalog1.getQuantity()); // 8 + 2 = 10
        assertEquals(5, catalog2.getQuantity()); // 2 + 3 = 5
        
        // Проверяем, что репозиторий был вызван для сохранения
        verify(catalogRepository).saveAll(anyList());
    }

    @Test
    void compensateOrder_SuccessfulCompensation_MissingProductInOrder() {
        // Given
        // В репозитории есть только один товар, хотя в заказе два
        catalog1.setQuantity(8);
        List<Catalog> catalogsSource = Collections.singletonList(catalog1);
        
        when(catalogRepository.findByShopIdAndProductIdIn(1L, Arrays.asList(1L, 2L)))
                .thenReturn(catalogsSource);
        
        // When
        WarehouseCompensationResponseEvent response = catalogService.compensateOrder(order, sagaId);
        
        // Then
        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals(sagaId, response.getSagaId());
        assertEquals(order, response.getOrder());
        assertEquals("Request NOT processed [not all positions reserved]", response.getMessage());
        
        // Проверяем, что количество товара 1 не изменилось
        assertEquals(8, catalog1.getQuantity());
        
        // Проверяем, что репозиторий НЕ был вызван для сохранения
        verify(catalogRepository, never()).saveAll(anyList());
    }

    @Test
    void compensateOrder_ExceptionDuringProcessing() {
        // Given
        when(catalogRepository.findByShopIdAndProductIdIn(1L, Arrays.asList(1L, 2L)))
                .thenThrow(new RuntimeException("Database error"));
        
        // When
        WarehouseCompensationResponseEvent response = catalogService.compensateOrder(order, sagaId);
        
        // Then
        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals(sagaId, response.getSagaId());
        assertEquals(order, response.getOrder());
        assertEquals("Database error", response.getMessage());
        
        // Проверяем, что репозиторий НЕ был вызван для сохранения
        verify(catalogRepository, never()).saveAll(anyList());
    }
}