package com.bida.repository;

import com.bida.entity.Product;
import com.bida.entity.enums.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrue();

    List<Product> findByActiveTrueOrderByNameAsc();

    List<Product> findByCategory(ProductCategory category);

    List<Product> findByCategoryAndActiveTrue(ProductCategory category);

    List<Product> findByStockQuantityLessThan(int threshold);

    List<Product> findByActiveTrueAndStockQuantityLessThan(int threshold);
}
