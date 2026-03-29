package com.bida.service;

import com.bida.entity.*;
import com.bida.repository.OrderItemRepository;
import com.bida.repository.ProductRepository;
import com.bida.repository.SessionRepository;
import com.bida.repository.UserRepository;
import com.bida.entity.enums.SessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderItemRepository orderItemRepository;
    private final SessionRepository sessionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    /**
     * Goi mon cho phien choi dang active.
     */
    public OrderItem addOrder(Long sessionId, Long productId, int quantity, String staffUsername) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay phien choi: " + sessionId));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new RuntimeException("Phien choi da ket thuc, khong the goi them mon");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay san pham: " + productId));

        if (!product.getActive()) {
            throw new RuntimeException("San pham da ngung ban: " + product.getName());
        }

        // Tru kho
        productService.reduceStock(productId, quantity);

        User staff = userRepository.findByUsername(staffUsername)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nhan vien: " + staffUsername));

        BigDecimal unitPrice = product.getPrice();
        BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(quantity));

        OrderItem item = OrderItem.builder()
                .session(session)
                .product(product)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .amount(amount)
                .orderedAt(LocalDateTime.now())
                .staff(staff)
                .build();

        item = orderItemRepository.save(item);
        log.info("Goi mon: {} x{} cho session #{} boi {}", product.getName(), quantity, sessionId, staffUsername);

        return item;
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getOrdersBySession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay phien choi: " + sessionId));
        return orderItemRepository.findBySessionOrderByOrderedAtDesc(session);
    }

    @Transactional(readOnly = true)
    public BigDecimal getServiceChargeBySession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay phien choi: " + sessionId));
        return orderItemRepository.sumAmountBySession(session);
    }

    @Transactional(readOnly = true)
    public long getOrderCountBySession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay phien choi: " + sessionId));
        return orderItemRepository.countBySession(session);
    }

    /**
     * Xoa order item (chi khi session con active).
     */
    public void removeOrder(Long orderItemId) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay order item: " + orderItemId));

        if (item.getSession().getStatus() != SessionStatus.ACTIVE) {
            throw new RuntimeException("Khong the xoa order cua phien da ket thuc");
        }

        // Tra lai kho
        productService.addStock(item.getProduct().getId(), item.getQuantity());

        orderItemRepository.delete(item);
        log.info("Xoa order item #{} (san pham: {}, so luong: {})",
                orderItemId, item.getProduct().getName(), item.getQuantity());
    }
}
