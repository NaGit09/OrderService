package com.furniro.OrderService.service;

import com.furniro.OrderService.database.repository.order.OrderItemRepository;
import com.furniro.OrderService.service.kafka.KafkaProducer;
import com.furniro.OrderService.utils.enums.OrderStatus;
import com.furniro.OrderService.utils.enums.PaymentMethod;
import com.furniro.OrderService.utils.enums.PaymentStatus;
import com.furniro.OrderService.utils.error.OrderErrorCode;
import com.furniro.OrderService.database.entity.order.Order;
import com.furniro.OrderService.database.entity.order.OrderItem;
import com.furniro.OrderService.database.entity.order.Payment;
import com.furniro.OrderService.database.repository.order.OrderRepository;
import com.furniro.OrderService.database.repository.order.PaymentRepository;
import com.furniro.OrderService.dto.API.AType;
import com.furniro.OrderService.dto.API.ApiType;
import com.furniro.OrderService.dto.API.ErrorType;
import com.furniro.OrderService.dto.req.CreateOrderReq;
import com.furniro.OrderService.dto.req.UpdateStatusOrder;
import com.furniro.OrderService.exception.OrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
        private final OrderRepository orderRepository;
        private final OrderItemRepository orderItemRepository;
        private final PaymentRepository paymentRepository;
        private final KafkaProducer producer;
        private final PayPalService payPalService;

        // create order for user
        @Transactional
        public ResponseEntity<AType> createOrder(CreateOrderReq orderReq) {

                // 1. Calculate total amount
                BigDecimal totalAmount = orderReq.getOrderItems().stream()
                                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .add(BigDecimal.valueOf(orderReq.getShippingFee()));

                // 2. Create order
                Order order = Order.builder()
                                .userID(orderReq.getUserID())
                                .orderNote(orderReq.getNote())
                                .address(orderReq.getAddress())
                                .shippingFee(BigDecimal.valueOf(orderReq.getShippingFee()))
                                .totalAmount(totalAmount)
                                .currency(orderReq.getCurrency())
                                .build();

                Order orderResult = orderRepository.save(order);

                // 3. Create order items
                List<OrderItem> orderItems = orderReq.getOrderItems().stream().map(
                                item -> OrderItem.builder()
                                                .variant(item.getVariantID())
                                                .quantity(item.getQuantity())
                                                .priceAtPurchase(item.getPrice())
                                                .order(orderResult)
                                                .build())
                                .toList();

                orderItemRepository.saveAll(orderItems);

                // 4. Create payment
                Payment payment = Payment.builder()
                                .amount(orderResult.getTotalAmount())
                                .paymentMethod(orderReq.getPaymentMethod())
                                .paymentStatus(orderReq.getPaymentStatus())
                                .currency(orderReq.getCurrency())
                                .order(orderResult)
                                .build();

                paymentRepository.save(payment);

                // 5. Handle PayPal
                Object data = true;

                if (orderReq.getPaymentMethod() == PaymentMethod.PAYPAL) {
                        Map<String, Object> paypalResponse = payPalService.createOrder(
                                        totalAmount.toPlainString(),
                                        orderReq.getCurrency());

                        String paypalOrderId = (String) paypalResponse.get("id");
                        payment.setPaypalOrderId(paypalOrderId);
                        paymentRepository.save(payment);

                        orderResult.setStatus(OrderStatus.CREATED);
                        orderRepository.save(orderResult);

                        data = paypalResponse;
                }

                // kafka sent message order created

                return ResponseEntity.ok(ApiType.success(data));
        }

        // capture paypal order
        @Transactional
        @SuppressWarnings("unchecked")
        public ResponseEntity<AType> capturePayPalOrder(String paypalOrderId) {

                Map<String, Object> captureResponse = payPalService.captureOrder(paypalOrderId);

                String status = (String) captureResponse.get("status");

                if ("COMPLETED".equals(status)) {

                        Payment payment = paymentRepository.findByPaypalOrderId(paypalOrderId)
                                        .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_EXIST));

                        payment.setPaymentStatus(PaymentStatus.PAID);

                        // Extract capture ID if needed
                        List<Map<String, Object>> purchaseUnits = (List<Map<String, Object>>) captureResponse
                                        .get("purchase_units");

                        if (purchaseUnits != null && !purchaseUnits.isEmpty()) {

                                Map<String, Object> payments = (Map<String, Object>) purchaseUnits.get(0)
                                                .get("payments");

                                if (payments != null) {
                                        List<Map<String, Object>> captures = (List<Map<String, Object>>) payments
                                                        .get("captures");

                                        if (captures != null && !captures.isEmpty()) {
                                                payment.setPaypalCaptureId((String) captures.get(0).get("id"));
                                        }
                                }
                        }

                        paymentRepository.save(payment);

                        Order order = payment.getOrder();
                        order.setStatus(OrderStatus.PAID);
                        orderRepository.save(order);

                        // kafka sent message order success

                        return ResponseEntity.ok(ApiType.success(true));
                }

                return ResponseEntity.status(400).body(ErrorType.badRequest("Payment capture failed"));
        }

        // change order status for admin approve or cancel order
        public ResponseEntity<AType> changeStatusOrder(UpdateStatusOrder updateStatusOrder) {

                Order order = orderRepository.findById(updateStatusOrder.getOrderID()).orElseThrow(
                                () -> new OrderException(OrderErrorCode.ORDER_NOT_EXIST));

                order.setStatus(updateStatusOrder.getStatus());

                orderRepository.save(order);

                // kafka sent message order success
                Map<String, Object> data = new HashMap<>();
                data.put("userId", order.getUserID());
                data.put("title", "Order " + updateStatusOrder.getStatus());
                data.put("content", "Order " + updateStatusOrder.getStatus() + " success");
                data.put("type", "UPDATE_ORDER_STATUS");
                
                producer.send("notification.created", data);
                return ResponseEntity.ok(ApiType.success(true));
        }

        // KAFKA EVENT HANDLER
        public void updateOrderStatus(Integer orderId, String status) {

                Order order = orderRepository.findById(orderId).orElseThrow(
                                () -> new OrderException(OrderErrorCode.ORDER_NOT_EXIST));

                order.setStatus(OrderStatus.valueOf(status));

                orderRepository.save(order);

                // kafka sent message order updated for message service
                Map<String, Object> data = new HashMap<>();
                data.put("userId", order.getUserID());
                data.put("title", "Order " + status);
                data.put("content", "Order " + status + " success");
                data.put("type", "UPDATE_ORDER_STATUS");

                producer.send("notification.created", data);

                log.info("Order {} updated successfully", orderId);
        }
}