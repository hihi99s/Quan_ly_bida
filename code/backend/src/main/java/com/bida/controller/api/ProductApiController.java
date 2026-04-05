package com.bida.controller.api;

import com.bida.entity.Product;
import com.bida.entity.enums.ProductCategory;
import com.bida.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API Controller cho Product - hỗ trợ dropdown order món.
 *
 * FIX: Thêm API endpoint để frontend dropdown load danh sách sản phẩm.
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductApiController {

    private final ProductService productService;

    /**
     * GET /api/products - Danh sách tất cả sản phẩm (active only).
     *
     * Response: JSON array của Product objects
     * [{
     *   "id": 1,
     *   "name": "Coca-Cola",
     *   "category": "DRINK",
     *   "price": 15000,
     *   "stockQuantity": 50,
     *   "imageUrl": "...",
     *   "active": true
     * }, ...]
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = productService.getActiveProducts();
            log.info("✓ GET /api/products - Trả về {} sản phẩm active", products.size());
            if (products.isEmpty()) {
                log.warn("⚠ Danh sách sản phẩm rỗng - kiểm tra DB seed hoặc ProductService");
            }
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("✗ LỖI GET /api/products: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/products/category/{category} - Sản phẩm theo danh mục.
     *
     * @param category DRINK, FOOD, SNACK, OTHER
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable ProductCategory category) {
        try {
            List<Product> products = productService.getByCategory(category);
            log.info("✓ GET /api/products/category/{} - Trả về {} sản phẩm", category, products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("✗ LỖI GET /api/products/category/{}: {}", category, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/products/{id} - Chi tiết một sản phẩm.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        try {
            Product product = productService.getById(id);
            log.debug("GET /api/products/{} - {}", id, product.getName());
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            log.warn("Product {} không tìm thấy: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Lỗi GET /api/products/{}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/products/low-stock - Sản phẩm còn ít tồn kho (< 5).
     *
     * Sử dụng để cảnh báo admin cần nhập hàng.
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        try {
            List<Product> products = productService.getLowStockProducts();
            log.debug("GET /api/products/low-stock - {} sản phẩm", products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Lỗi GET /api/products/low-stock: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * POST /api/products - Thêm sản phẩm mới.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN thêm sản phẩm
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        try {
            Product created = productService.createProduct(
                    product.getName(),
                    product.getCategory(),
                    product.getPrice(),
                    product.getStockQuantity(),
                    product.getImageUrl()
            );
            return ResponseEntity.ok(Map.of("success", true, "message", "Thêm sản phẩm thành công", "product", created));
        } catch (Exception e) {
            log.error("Lỗi thêm sản phẩm: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * PUT /api/products/{id} - Cập nhật sản phẩm.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN sửa sản phẩm
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            Product updated = productService.updateProduct(
                    id,
                    product.getName(),
                    product.getCategory(),
                    product.getPrice(),
                    product.getStockQuantity(),
                    product.getImageUrl()
            );
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật thành công", "product", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * DELETE /api/products/{id} - Xóa sản phẩm.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN xóa sản phẩm
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa sản phẩm thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
