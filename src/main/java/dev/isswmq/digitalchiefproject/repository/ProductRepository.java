package dev.isswmq.digitalchiefproject.repository;

import dev.isswmq.digitalchiefproject.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.skus s WHERE s.availableFrom > :date")
    List<Product> findByAvailableFromAfter(@Param("date") LocalDate date);

    List<Product> findAll();
}
