package ru.binarysimple.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.binarysimple.warehouse.model.ProcessedEventId;

public interface ProcessedEventIdRepository extends JpaRepository<ProcessedEventId, String> {
}
