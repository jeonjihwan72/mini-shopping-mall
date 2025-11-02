package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.ProductCreateRequest;
import org.example.dto.ProductResponse;
import org.example.dto.ProductSimpleResponse;
import org.example.dto.ProductUpdateRequest;
import org.example.entity.Product;
import org.example.exception.BusinessException;
import org.example.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.example.repository.OrderItemRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .description(request.getDescription())
                .build();
        Product savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    public Page<ProductSimpleResponse> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductSimpleResponse::from);
    }

    public ProductResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
        product.update(request.getName(), request.getPrice(), request.getStock(), request.getDescription());
        return ProductResponse.from(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
        if (orderItemRepository.existsByProduct(product)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Product is referenced by an order item");
        }
        productRepository.delete(product);
    }
}
