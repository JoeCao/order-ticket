package com.orderticket.repository;

import com.orderticket.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find by order number
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // Find orders by customer name (case insensitive, partial match)
    Page<Order> findByCustomerNameContainingIgnoreCase(String customerName, Pageable pageable);
    
    // Find orders by status
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    // Find orders by customer email
    List<Order> findByCustomerEmail(String customerEmail);
    
    // Find orders within date range
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    Page<Order> findByOrderDateBetween(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate, 
                                      Pageable pageable);
    
    // Complex search query with multiple filters
    @Query("SELECT o FROM Order o WHERE " +
           "(:orderNumber IS NULL OR o.orderNumber LIKE %:orderNumber%) AND " +
           "(:customerName IS NULL OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:startDate IS NULL OR o.orderDate >= :startDate) AND " +
           "(:endDate IS NULL OR o.orderDate <= :endDate) " +
           "ORDER BY o.orderDate DESC")
    Page<Order> findOrdersWithFilters(@Param("orderNumber") String orderNumber,
                                     @Param("customerName") String customerName,
                                     @Param("status") Order.OrderStatus status,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     Pageable pageable);
    
    // Find recent orders
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    Page<Order> findRecentOrders(Pageable pageable);
    
    // Count orders by status
    long countByStatus(Order.OrderStatus status);
    
    // Find orders for export (without pagination)
    @Query("SELECT o FROM Order o WHERE " +
           "(:orderNumber IS NULL OR o.orderNumber LIKE %:orderNumber%) AND " +
           "(:customerName IS NULL OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:startDate IS NULL OR o.orderDate >= :startDate) AND " +
           "(:endDate IS NULL OR o.orderDate <= :endDate) " +
           "ORDER BY o.orderDate DESC")
    List<Order> findOrdersForExport(@Param("orderNumber") String orderNumber,
                                   @Param("customerName") String customerName,
                                   @Param("status") Order.OrderStatus status,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);
}