package ru.binarysimple.warehouse.filter;

import org.springframework.data.jpa.domain.Specification;
import ru.binarysimple.warehouse.model.Catalog;

import java.time.LocalDateTime;

public record CatalogFilter(Long id, Long shopId, Boolean active, LocalDateTime createdAtLte,
                            LocalDateTime createdAtGte, LocalDateTime updatedAtLte, LocalDateTime updatedAtGte) {
    public Specification<Catalog> toSpecification() {
        return idSpec()
                .and(shopIdSpec())
                .and(activeSpec())
                .and(createdAtLteSpec())
                .and(createdAtGteSpec())
                .and(updatedAtLteSpec())
                .and(updatedAtGteSpec());
    }

    private Specification<Catalog> idSpec() {
        return ((root, query, cb) -> id != null
                ? cb.equal(root.get("id"), id)
                : null);
    }

    private Specification<Catalog> shopIdSpec() {
        return ((root, query, cb) -> shopId != null
                ? cb.equal(root.get("shopId"), shopId)
                : null);
    }

    private Specification<Catalog> activeSpec() {
        return ((root, query, cb) -> active != null
                ? cb.equal(root.get("active"), active)
                : null);
    }

    private Specification<Catalog> createdAtLteSpec() {
        return ((root, query, cb) -> createdAtLte != null
                ? cb.lessThanOrEqualTo(root.get("createdAt"), createdAtLte)
                : null);
    }

    private Specification<Catalog> createdAtGteSpec() {
        return ((root, query, cb) -> createdAtGte != null
                ? cb.greaterThanOrEqualTo(root.get("createdAt"), createdAtGte)
                : null);
    }

    private Specification<Catalog> updatedAtLteSpec() {
        return ((root, query, cb) -> updatedAtLte != null
                ? cb.lessThanOrEqualTo(root.get("updatedAt"), updatedAtLte)
                : null);
    }

    private Specification<Catalog> updatedAtGteSpec() {
        return ((root, query, cb) -> updatedAtGte != null
                ? cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedAtGte)
                : null);
    }
}