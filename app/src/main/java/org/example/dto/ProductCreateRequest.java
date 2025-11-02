package org.example.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCreateRequest {
    private String name;
    private int price;
    private int stock;
    private String description;
}
