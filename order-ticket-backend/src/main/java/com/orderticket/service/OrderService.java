package com.orderticket.service;

import com.orderticket.entity.Order;
import com.orderticket.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    // Find all orders with pagination
    public Page<Order> findAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
    
    // Find order by ID
    public Optional<Order> findOrderById(Long id) {
        return orderRepository.findById(id);
    }
    
    // Find order by order number
    public Optional<Order> findOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }
    
    // Search orders with multiple filters
    public Page<Order> searchOrders(String orderNumber, String customerName, 
                                   Order.OrderStatus status, LocalDateTime startDate,
                                   LocalDateTime endDate, Pageable pageable) {
        return orderRepository.findOrdersWithFilters(
            orderNumber, customerName, status, startDate, endDate, pageable);
    }
    
    // Get orders for export (without pagination)
    public List<Order> getOrdersForExport(String orderNumber, String customerName,
                                         Order.OrderStatus status, LocalDateTime startDate,
                                         LocalDateTime endDate) {
        return orderRepository.findOrdersForExport(
            orderNumber, customerName, status, startDate, endDate);
    }
    
    // Find orders by customer name
    public Page<Order> findOrdersByCustomerName(String customerName, Pageable pageable) {
        return orderRepository.findByCustomerNameContainingIgnoreCase(customerName, pageable);
    }
    
    // Find orders by status
    public Page<Order> findOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }
    
    // Find orders within date range
    public Page<Order> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, 
                                           Pageable pageable) {
        return orderRepository.findByOrderDateBetween(startDate, endDate, pageable);
    }
    
    // Get recent orders
    public Page<Order> getRecentOrders(Pageable pageable) {
        return orderRepository.findRecentOrders(pageable);
    }
    
    // Create new order
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }
    
    // Update existing order
    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }
    
    // Delete order
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
    
    // Get order statistics
    public OrderStatistics getOrderStatistics() {
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        long processingOrders = orderRepository.countByStatus(Order.OrderStatus.PROCESSING);
        long deliveredOrders = orderRepository.countByStatus(Order.OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByStatus(Order.OrderStatus.CANCELLED);
        
        return new OrderStatistics(totalOrders, pendingOrders, processingOrders, 
                                 deliveredOrders, cancelledOrders);
    }
    
    // Inner class for order statistics
    public static class OrderStatistics {
        private long totalOrders;
        private long pendingOrders;
        private long processingOrders;
        private long deliveredOrders;
        private long cancelledOrders;
        
        public OrderStatistics(long totalOrders, long pendingOrders, long processingOrders,
                             long deliveredOrders, long cancelledOrders) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.processingOrders = processingOrders;
            this.deliveredOrders = deliveredOrders;
            this.cancelledOrders = cancelledOrders;
        }
        
        // Getters
        public long getTotalOrders() { return totalOrders; }
        public long getPendingOrders() { return pendingOrders; }
        public long getProcessingOrders() { return processingOrders; }
        public long getDeliveredOrders() { return deliveredOrders; }
        public long getCancelledOrders() { return cancelledOrders; }
    }
}