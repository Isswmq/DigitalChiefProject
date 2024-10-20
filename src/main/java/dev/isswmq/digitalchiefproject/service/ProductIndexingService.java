package dev.isswmq.digitalchiefproject.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import dev.isswmq.digitalchiefproject.model.Product;
import dev.isswmq.digitalchiefproject.model.Sku;
import dev.isswmq.digitalchiefproject.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductIndexingService {

    private final ProductRepository productRepository;
    private final ElasticsearchClient elasticClient;

    public void loadDataToElastic(LocalDate filterDate) throws IOException {

        List<Product> products = (filterDate == null)
                ? productRepository.findAll()
                : productRepository.findByAvailableFromAfter(filterDate);

        log.info("Products found: {}", products.size());

        for (Product product : products) {
            log.info("Processing product: {}", product.getName());

            for (Sku sku : product.getSkus()) {
                log.info("Indexing SKU: {}, Product: {}", sku.getId(), product.getName());
                Map<String, Object> jsonMap = getStringObjectMap(product, sku);
                IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                        .index("product-sku-index")
                        .id(String.valueOf(sku.getId()))
                        .document(jsonMap)
                );

                try {
                    elasticClient.index(request);
                    log.info("Successfully indexed SKU: {}", sku.getId());
                } catch (Exception e) {
                    log.error("Failed to index SKU: {}, Error: {}", sku.getId(), e.getMessage(), e);
                }
            }
        }
    }


    private static Map<String, Object> getStringObjectMap(Product product, Sku sku) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("productId", product.getId());
        jsonMap.put("productName", product.getName());
        jsonMap.put("description", product.getDescription());
        jsonMap.put("skuId", sku.getId());
        jsonMap.put("skuCode", sku.getSkuCode());
        jsonMap.put("price", sku.getPrice());
        jsonMap.put("stockQuantity", sku.getStockQuantity());
        jsonMap.put("availableFrom", sku.getAvailableFrom().toString());
        return jsonMap;
    }
}
