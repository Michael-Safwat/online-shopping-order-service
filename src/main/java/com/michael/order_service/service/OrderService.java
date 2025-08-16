package com.michael.order_service.service;

import com.michael.order_service.client.InventoryClient;
import com.michael.order_service.dto.OrderRequest;
import com.michael.order_service.model.Order;
import com.michael.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public String placeOrder(OrderRequest orderRequest){
        var isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());

        if(isProductInStock) {
            Order order = new Order();
            order.setId(orderRequest.id());
            order.setOrderNumber(orderRequest.orderNumber());
            order.setSkuCode(orderRequest.skuCode());
            order.setPrice(orderRequest.price());
            order.setQuantity(orderRequest.quantity());

            orderRepository.save(order);
            log.info("order placed successfully");
            return "order placed successfully";
        }else {
            return "Product with SkuCode: "+ orderRequest.skuCode()+" is not in stock";
        }

    }
}
