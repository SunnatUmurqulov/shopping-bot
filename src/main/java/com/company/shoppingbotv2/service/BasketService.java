package com.company.shoppingbotv2.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.company.shoppingbotv2.entity.OrderProduct;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.OrderProductStatus;
import com.company.shoppingbotv2.payload.GetProduct;
import com.company.shoppingbotv2.repository.OrderProductRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BasketService {
    private final OrderProductRepository orderProductRepository;

    public void addProduct(String name, Double price, String productId, double remainder, double count, User user) {
        Optional<OrderProduct> optionalBasket = orderProductRepository.findByProductIdAndUserIdAndStatus(
                productId,
                user.getId(),
                OrderProductStatus.NEW);
        OrderProduct orderProduct;
        if (optionalBasket.isEmpty()) {
            orderProduct = new OrderProduct();
            orderProduct.setUser(user);
            orderProduct.setProductId(productId);
            orderProduct.setProductName(name);
            orderProduct.setProductPrice(price);
            orderProduct.setRemainder(remainder);
            orderProduct.setCount(count);
            orderProduct.setAmount(((long) count * price));
            orderProduct.setStatus(OrderProductStatus.NEW);
        } else {
            orderProduct = optionalBasket.get();
            orderProduct.setCount(orderProduct.getCount() + count);
            orderProduct.setAmount(orderProduct.getProductPrice() * orderProduct.getCount());
        }
        orderProductRepository.save(orderProduct);
    }

    public List<OrderProduct> userShoppingCart(User user) {
        return orderProductRepository.findByUserAndStatus(user, OrderProductStatus.NEW, Pageable.unpaged(Sort.by("id").descending()));
    }

    public OrderProduct getBasketByProduct(String productId) {
        Optional<OrderProduct> optionalBasket = orderProductRepository.findByProductIdAndStatus(productId, OrderProductStatus.NEW);
        return optionalBasket.orElse(null);
    }

    public Long getTotalUserCartAmount(User user) {
        return orderProductRepository.getSumBasketAmount(user);
    }

    public void updateBasketCount(OrderProduct orderProduct, double count) {
        orderProduct.setCount(count);
        orderProduct.setAmount(orderProduct.getCount() * orderProduct.getProductPrice());
        orderProductRepository.save(orderProduct);
    }

    public OrderProduct createTemporaryOrder(User user, GetProduct product) {
        OrderProduct orderProduct = new OrderProduct(user,
                product.id(),
                product.name(),
                product.price(),
                product.remainder(),
                1,
                null,
                product.price(),
                OrderProductStatus.TEMPORARY);
        return orderProductRepository.save(orderProduct);
    }

    public OrderProduct getById(Integer id, User user) {
        return orderProductRepository.findByIdAndUserIdAndStatus(id, user.getId(), OrderProductStatus.TEMPORARY)
                .orElseThrow(() -> new NotFoundException("Product id not found"));
    }

    public OrderProduct getProductStatusNew(Integer productId) {
        return orderProductRepository.findByIdAndStatus(productId, OrderProductStatus.NEW);
    }

    public Page<OrderProduct> userOrders(User user,Pageable pageable) {
        return orderProductRepository.findAllByUserIdAndStatus(user.getId(), OrderProductStatus.ORDERED,pageable);
    }

    public OrderProduct getProductStatusOrder(User user, int productId) {
        return orderProductRepository.findOrderProductByIdAndUserIdAndStatus(productId, user.getId(), OrderProductStatus.ORDERED)
                .orElseThrow(() -> new NotFoundException("order product id not found"));
    }

    public boolean orderProductByUserId(Integer id) {
        return orderProductRepository.existsOrderProductByUserIdAndStatus(id,OrderProductStatus.ORDERED);
    }
}
