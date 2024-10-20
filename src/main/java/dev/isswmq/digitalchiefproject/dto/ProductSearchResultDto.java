package dev.isswmq.digitalchiefproject.dto;

import lombok.Data;

@Data
public class ProductSearchResultDto {
    private Long productId;
    private String productName;
    private String description;
    private Long skuId;
    private String skuCode;
    private Double price;
    private Integer stockQuantity;
    private String availableFrom;
}
