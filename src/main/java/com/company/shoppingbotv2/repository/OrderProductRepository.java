package com.company.shoppingbotv2.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.company.shoppingbotv2.entity.OrderProduct;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.OrderProductStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, Integer> {
    Optional<OrderProduct> findByProductIdAndUserIdAndStatus(String productId, Integer user_id, OrderProductStatus orderProductStatus);

    List<OrderProduct> findByUserAndStatus(User user, OrderProductStatus orderProductStatus, Pageable pageable);

    Optional<OrderProduct> findByProductIdAndStatus(String productId, OrderProductStatus orderProductStatus);

    Optional<OrderProduct> findByIdAndUserIdAndStatus(Integer id, Integer user_id, OrderProductStatus status);

    OrderProduct findByIdAndStatus(Integer id, OrderProductStatus status);

    Page<OrderProduct> findAllByUserIdAndStatus(Integer user_id, OrderProductStatus status, Pageable pageable);

    Optional<OrderProduct> findOrderProductByIdAndUserIdAndStatus(Integer id, Integer user_id, OrderProductStatus status);

    boolean existsOrderProductByUserIdAndStatus(Integer user_id, OrderProductStatus status);

    @Query("SELECT sum(b.amount) from OrderProduct b where b.user = :user AND b.status = 'NEW'")
    Long getSumBasketAmount(User user);
}
