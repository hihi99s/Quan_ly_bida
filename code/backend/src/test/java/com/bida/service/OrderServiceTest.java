package com.bida.service;

import com.bida.entity.*;
import com.bida.entity.enums.ProductCategory;
import com.bida.entity.enums.SessionStatus;
import com.bida.repository.OrderItemRepository;
import com.bida.repository.ProductRepository;
import com.bida.repository.SessionRepository;
import com.bida.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private Session activeSession;
    private Product activeProduct;
    private User staff;

    @BeforeEach
    void setUp() {
        activeSession = Session.builder().id(1L).status(SessionStatus.ACTIVE).build();
        activeProduct = Product.builder()
                .id(10L)
                .name("Sting")
                .price(new BigDecimal("15000"))
                .active(true)
                .stockQuantity(100)
                .category(ProductCategory.DRINK)
                .build();
        staff = User.builder().id(1L).username("staff1").build();
    }

    @Test
    void addOrder_Success() {
        // Arrange
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(activeSession));
        when(productRepository.findById(10L)).thenReturn(Optional.of(activeProduct));
        when(userRepository.findByUsername("staff1")).thenReturn(Optional.of(staff));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        OrderItem item = orderService.addOrder(1L, 10L, 2, "staff1");

        // Assert
        assertNotNull(item);
        assertEquals(activeProduct.getName(), item.getProduct().getName());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("30000"), item.getAmount());
        
        verify(productService).reduceStock(10L, 2);
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    void addOrder_Fail_SessionNotActive() {
        // Arrange
        activeSession.setStatus(SessionStatus.COMPLETED);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(activeSession));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> 
            orderService.addOrder(1L, 10L, 1, "staff1")
        );
        assertTrue(exception.getMessage().contains("Phien choi da ket thuc"));
        verifyNoInteractions(productService, orderItemRepository);
    }

    @Test
    void removeOrder_Success() {
        // Arrange
        OrderItem item = OrderItem.builder()
                .id(50L)
                .session(activeSession)
                .product(activeProduct)
                .quantity(3)
                .build();
        when(orderItemRepository.findById(50L)).thenReturn(Optional.of(item));

        // Act
        orderService.removeOrder(50L);

        // Assert
        verify(productService).addStock(10L, 3);
        verify(orderItemRepository).delete(item);
    }
}
