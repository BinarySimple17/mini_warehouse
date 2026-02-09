package ru.binarysimple.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.binarysimple.warehouse.model.Catalog;

import java.util.List;

public interface CatalogRepository extends JpaRepository<Catalog, Long>, JpaSpecificationExecutor<Catalog> {

    List<Catalog> findByShopId(Long shopId);

    Catalog findByShopIdAndProductId(Long shopId, Long productId);

    List<Catalog> findByShopIdAndProductIdIn(Long shopId, List<Long> productIds);

    
}