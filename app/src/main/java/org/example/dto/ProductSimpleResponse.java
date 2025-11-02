package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.entity.Product;

@Getter
@Builder
@AllArgsConstructor
public class ProductSimpleResponse {
    private Long id;
    private String name;
    private int price;

    public static ProductSimpleResponse from(Product product) {
        return ProductSimpleResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .build();
    }
}
