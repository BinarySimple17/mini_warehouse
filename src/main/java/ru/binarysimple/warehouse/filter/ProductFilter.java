package ru.binarysimple.warehouse.filter;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.binarysimple.warehouse.model.Product;

import java.time.LocalDateTime;

public record ProductFilter(String nameLike, String skuLike, LocalDateTime createdAtLte, LocalDateTime createdAtGte,
                            LocalDateTime updatedAtLte, LocalDateTime updatedAtGte) {
    public Specification<Product> toSpecification() {
        return nameLikeSpec()
                .and(skuLikeSpec())
                .and(createdAtLteSpec())
                .and(createdAtGteSpec())
                .and(updatedAtLteSpec())
                .and(updatedAtGteSpec());
    }

    private Specification<Product> nameLikeSpec() {
        return ((root, query, cb) -> StringUtils.hasText(nameLike)
                ? cb.like(cb.lower(root.get("name")), nameLike.toLowerCase())
                : null);
    }

    private Specification<Product> skuLikeSpec() {
        return ((root, query, cb) -> StringUtils.hasText(skuLike)
                ? cb.like(cb.lower(root.get("sku")), skuLike.toLowerCase())
                : null);
    }

    private Specification<Product> createdAtLteSpec() {
        return ((root, query, cb) -> createdAtLte != null
                ? cb.lessThanOrEqualTo(root.get("createdAt"), createdAtLte)
                : null);
    }

    private Specification<Product> createdAtGteSpec() {
        return ((root, query, cb) -> createdAtGte != null
                ? cb.greaterThanOrEqualTo(root.get("createdAt"), createdAtGte)
                : null);
    }

    private Specification<Product> updatedAtLteSpec() {
        return ((root, query, cb) -> updatedAtLte != null
                ? cb.lessThanOrEqualTo(root.get("updatedAt"), updatedAtLte)
                : null);
    }

    private Specification<Product> updatedAtGteSpec() {
        return ((root, query, cb) -> updatedAtGte != null
                ? cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedAtGte)
                : null);
    }
}