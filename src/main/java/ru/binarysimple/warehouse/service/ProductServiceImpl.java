package ru.binarysimple.warehouse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.binarysimple.warehouse.dto.ProductDto;
import ru.binarysimple.warehouse.filter.ProductFilter;
import ru.binarysimple.warehouse.mapper.ProductMapper;
import ru.binarysimple.warehouse.model.Product;
import ru.binarysimple.warehouse.repository.ProductRepository;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    private final ProductRepository productRepository;

    private final ObjectMapper objectMapper;

    @Override
    public Page<ProductDto> getAll(ProductFilter filter, Pageable pageable) {
        Specification<Product> spec = filter.toSpecification();
        Page<Product> products = productRepository.findAll(spec, pageable);
        return products.map(productMapper::toProductDto);
    }

    @Override
    public ProductDto getOne(Long id) {
        Optional<Product> productOptional = productRepository.findById(id);
        return productMapper.toProductDto(productOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id))));
    }

    @Override
    public List<ProductDto> getMany(List<Long> ids) {
        List<Product> products = productRepository.findAllById(ids);
        return products.stream()
                .map(productMapper::toProductDto)
                .toList();
    }

    @Override
    public ProductDto create(ProductDto dto) {
        Product product = productMapper.toEntity(dto);
        Product resultProduct = productRepository.save(product);
        return productMapper.toProductDto(resultProduct);
    }

    @Override
    public ProductDto patch(Long id, JsonNode patchNode) throws IOException {
        Product product = productRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        ProductDto productDto = productMapper.toProductDto(product);
        objectMapper.readerForUpdating(productDto).readValue(patchNode);
        productMapper.updateWithNull(productDto, product);

        Product resultProduct = productRepository.save(product);
        return productMapper.toProductDto(resultProduct);
    }

    @Override
    public List<Long> patchMany(List<Long> ids, JsonNode patchNode) throws IOException {
        Collection<Product> products = productRepository.findAllById(ids);

        for (Product product : products) {
            ProductDto productDto = productMapper.toProductDto(product);
            objectMapper.readerForUpdating(productDto).readValue(patchNode);
            productMapper.updateWithNull(productDto, product);
        }

        List<Product> resultProducts = productRepository.saveAll(products);
        return resultProducts.stream()
                .map(Product::getId)
                .toList();
    }

    @Override
    public ProductDto delete(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            productRepository.delete(product);
        }
        return productMapper.toProductDto(product);
    }

    @Override
    public void deleteMany(List<Long> ids) {
        productRepository.deleteAllById(ids);
    }
}
