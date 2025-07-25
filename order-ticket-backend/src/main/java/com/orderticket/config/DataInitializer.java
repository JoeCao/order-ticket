package com.orderticket.config;

import com.orderticket.entity.Order;
import com.orderticket.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (orderRepository.count() == 0) {
            initializeOrders();
        }
    }
    
    private void initializeOrders() {
        List<Order> sampleOrders = Arrays.asList(
            createOrder("ORD-2024-001", "张三", "zhangsan@email.com", "13800138001", 
                       new BigDecimal("299.99"), Order.OrderStatus.DELIVERED,
                       "电子产品订单", "iPhone 15 Pro Max - 256GB 深空黑色"),
            
            createOrder("ORD-2024-002", "李四", "lisi@email.com", "13800138002", 
                       new BigDecimal("1299.50"), Order.OrderStatus.PROCESSING,
                       "家具订单", "实木书桌 + 人体工学椅套装"),
            
            createOrder("ORD-2024-003", "王五", "wangwu@email.com", "13800138003", 
                       new BigDecimal("89.90"), Order.OrderStatus.SHIPPED,
                       "服装订单", "春季新款连衣裙 - 蓝色 M码"),
            
            createOrder("ORD-2024-004", "赵六", "zhaoliu@email.com", "13800138004", 
                       new BigDecimal("2999.00"), Order.OrderStatus.CONFIRMED,
                       "电器订单", "海尔变频空调 1.5匹"),
            
            createOrder("ORD-2024-005", "孙七", "sunqi@email.com", "13800138005", 
                       new BigDecimal("599.99"), Order.OrderStatus.PENDING,
                       "运动用品", "耐克Air Jordan篮球鞋 - 42码"),
            
            createOrder("ORD-2024-006", "周八", "zhouba@email.com", "13800138006", 
                       new BigDecimal("159.90"), Order.OrderStatus.CANCELLED,
                       "美妆用品", "SK-II神仙水 230ml"),
                       
            createOrder("ORD-2024-007", "吴九", "wujiu@email.com", "13800138007", 
                       new BigDecimal("3999.00"), Order.OrderStatus.DELIVERED,
                       "数码产品", "MacBook Air M2 - 512GB 星光色"),
            
            createOrder("ORD-2024-008", "郑十", "zhengshi@email.com", "13800138008", 
                       new BigDecimal("799.99"), Order.OrderStatus.PROCESSING,
                       "厨房电器", "九阳豆浆机 + 空气炸锅套装")
        );
        
        orderRepository.saveAll(sampleOrders);
        System.out.println("Sample orders initialized successfully!");
    }
    
    private Order createOrder(String orderNumber, String customerName, String email, 
                             String phone, BigDecimal amount, Order.OrderStatus status,
                             String description, String productDetails) {
        Order order = new Order(orderNumber, customerName, email, phone, amount, status, description, productDetails);
        order.setOrderDate(LocalDateTime.now().minusDays((int)(Math.random() * 30)));
        return order;
    }
}