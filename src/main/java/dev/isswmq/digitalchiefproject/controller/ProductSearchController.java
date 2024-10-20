package dev.isswmq.digitalchiefproject.controller;

import dev.isswmq.digitalchiefproject.model.Product;
import dev.isswmq.digitalchiefproject.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @GetMapping("/search")
    public ResponseEntity<List<Product>> search(@RequestParam("keyword") String keyword){
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("Search keyword is empty or null");
            return ResponseEntity.badRequest().body(null);
        }

        log.info("Searching for products with keyword: {}", keyword);
        List<Product> products = productSearchService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }
}
