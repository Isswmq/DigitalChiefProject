package dev.isswmq.digitalchiefproject.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import dev.isswmq.digitalchiefproject.dto.ProductSearchResultDto;
import dev.isswmq.digitalchiefproject.model.Product;
import dev.isswmq.digitalchiefproject.model.Sku;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {

    @Value("${filter.enabled}")
    private boolean filterEnabled;

    @Value("${filter.availableFrom}")
    private LocalDate filterAvailableFrom;

    @Value("${filter.stockQuantity}")
    private int filterStockQuantity;

    private final ElasticsearchClient elasticClient;

    public List<Product> searchProducts(String keyword) {
        log.info("Searching for products with keyword: {}", keyword);

        SearchRequest searchRequest = buildSearchRequest(keyword);
        SearchResponse<ProductSearchResultDto> response = executeSearch(searchRequest);

        if (response == null || response.hits().hits().isEmpty()) {
            log.info("No products found for keyword: {}", keyword);
            return Collections.emptyList();
        }

        Map<Long, Product> productMap = buildProductMap(response);

        if (filterEnabled) {
            log.info("Filtering products based on enabled filters.");
            return filterProducts(productMap);
        }

        return new ArrayList<>(productMap.values());
    }

    private SearchRequest buildSearchRequest(String keyword) {
        return SearchRequest.of(s -> s
                .index("product-sku-index")
                .query(Query.of(q -> q
                        .multiMatch(m -> m
                                .query(keyword)
                                .fields("productName^4", "description^2", "skuCode^1")))));
    }

    private SearchResponse<ProductSearchResultDto> executeSearch(SearchRequest searchRequest) {
        try {
            return elasticClient.search(searchRequest, ProductSearchResultDto.class);
        } catch (Exception e) {
            log.error("Error executing search: {}", e.getMessage());
            return null;
        }
    }

    private Map<Long, Product> buildProductMap(SearchResponse<ProductSearchResultDto> response) {
        Map<Long, Product> productMap = new HashMap<>();

        for (Hit<ProductSearchResultDto> hit : response.hits().hits()) {
            ProductSearchResultDto dto = hit.source();
            if (dto != null) {
                productMap.computeIfAbsent(dto.getProductId(), id -> createProduct(dto));
                addSkuToProduct(productMap.get(dto.getProductId()), dto);
            }
        }
        return productMap;
    }

    private Product createProduct(ProductSearchResultDto dto) {
        Product product = new Product();
        product.setId(dto.getProductId());
        product.setName(dto.getProductName());
        product.setDescription(dto.getDescription());
        return product;
    }

    private void addSkuToProduct(Product product, ProductSearchResultDto dto) {
        Sku sku = new Sku();
        sku.setId(dto.getSkuId());
        sku.setSkuCode(dto.getSkuCode());
        sku.setPrice(BigDecimal.valueOf(dto.getPrice()));
        sku.setStockQuantity(dto.getStockQuantity());
        sku.setAvailableFrom(LocalDate.parse(dto.getAvailableFrom()));
        product.getSkus().add(sku);
    }

    private List<Product> filterProducts(Map<Long, Product> productMap) {
        return productMap.values().stream()
                .map(product -> {
                    List<Sku> filteredSkus = product.getSkus().stream()
                            .filter(sku -> sku.getAvailableFrom().isBefore(filterAvailableFrom)
                                    && sku.getStockQuantity() >= filterStockQuantity)
                            .collect(Collectors.toList());

                    if (!filteredSkus.isEmpty()) {
                        product.setSkus(filteredSkus);
                        return product;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
