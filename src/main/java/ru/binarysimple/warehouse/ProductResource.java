package ru.binarysimple.warehouse;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;
import ru.binarysimple.warehouse.dto.ProductDto;
import ru.binarysimple.warehouse.filter.ProductFilter;
import ru.binarysimple.warehouse.service.ProductService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/warehouse/products")
@RequiredArgsConstructor
public class ProductResource {

    private final ProductService productService;

    @GetMapping
    public PagedModel<ProductDto> getAll(@ParameterObject @ModelAttribute ProductFilter filter, @ParameterObject Pageable pageable) {
        Page<ProductDto> productDtos = productService.getAll(filter, pageable);
        return new PagedModel<>(productDtos);
    }

    @GetMapping("/{id}")
    public ProductDto getOne(@PathVariable Long id) {
        return productService.getOne(id);
    }

    @GetMapping("/by-ids")
    public List<ProductDto> getMany(@RequestParam List<Long> ids) {
        return productService.getMany(ids);
    }

    @PostMapping
    public ProductDto create(@RequestBody @Valid ProductDto dto) {
        return productService.create(dto);
    }

    @PatchMapping("/{id}")
    public ProductDto patch(@PathVariable Long id, @RequestBody JsonNode patchNode) throws IOException {
        return productService.patch(id, patchNode);
    }

//    @PatchMapping
//    public List<Long> patchMany(@RequestParam List<Long> ids, @RequestBody JsonNode patchNode) throws IOException {
//        return productService.patchMany(ids, patchNode);
//    }

    @DeleteMapping("/{id}")
    public ProductDto delete(@PathVariable Long id) {
        return productService.delete(id);
    }

    @DeleteMapping
    public void deleteMany(@RequestParam List<Long> ids) {
        productService.deleteMany(ids);
    }
}
