package ru.binarysimple.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.binarysimple.warehouse.model.Catalog;

public interface CatalogRepository extends JpaRepository<Catalog, Long> {
}