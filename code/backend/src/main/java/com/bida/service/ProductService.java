package com.bida.service;

import com.bida.entity.Product;
import com.bida.entity.enums.ProductCategory;
import com.bida.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Product> getByCategory(ProductCategory category) {
        return productRepository.findByCategoryAndActiveTrue(category);
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay san pham: " + id));
    }

    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        return productRepository.findByActiveTrueAndStockQuantityLessThan(5);
    }

    public Product createProduct(String name, ProductCategory category, BigDecimal price,
                                  int stockQuantity, String imageUrl) {
        Product product = Product.builder()
                .name(name)
                .category(category)
                .price(price)
                .stockQuantity(stockQuantity)
                .imageUrl(imageUrl)
                .active(true)
                .build();
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, String name, ProductCategory category, BigDecimal price,
                                  int stockQuantity, String imageUrl) {
        Product product = getById(id);
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setImageUrl(imageUrl);
        return productRepository.save(product);
    }

    public void toggleActive(Long id) {
        Product product = getById(id);
        product.setActive(!product.getActive());
        productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * Tru kho khi dat mon.
     */
    public void reduceStock(Long productId, int quantity) {
        Product product = getById(productId);
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Khong du ton kho cho san pham: " + product.getName());
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }

    /**
     * Them ton kho (nhap hang).
     */
    public void addStock(Long productId, int quantity) {
        Product product = getById(productId);
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);
    }
}
