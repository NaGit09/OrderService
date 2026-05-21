package com.furniro.OrderService.service;

import com.furniro.OrderService.database.entity.cart.Cart;
import com.furniro.OrderService.database.entity.cart.CartItem;
import com.furniro.OrderService.database.entity.order.Order;
import com.furniro.OrderService.database.entity.order.OrderItem;
import com.furniro.OrderService.database.entity.order.Payment;
import com.furniro.OrderService.database.repository.cart.CartRepository;
import com.furniro.OrderService.database.repository.order.OrderItemRepository;
import com.furniro.OrderService.database.repository.order.OrderRepository;
import com.furniro.OrderService.database.repository.order.PaymentRepository;
import com.furniro.OrderService.dto.API.AType;
import com.furniro.OrderService.dto.API.ApiType;
import com.furniro.OrderService.dto.API.ErrorType;
import com.furniro.OrderService.dto.req.CreateOrderReq;
import com.furniro.OrderService.dto.req.OrderItemReq;
import com.furniro.OrderService.dto.req.UpdateStatusOrder;
import com.furniro.OrderService.exception.CartException;
import com.furniro.OrderService.exception.OrderException;
import com.furniro.OrderService.service.kafka.KafkaProducer;
import com.furniro.OrderService.utils.enums.OrderStatus;
import com.furniro.OrderService.utils.enums.PaymentMethod;
import com.furniro.OrderService.utils.enums.PaymentStatus;
import com.furniro.OrderService.utils.error.CartErrorCode;
import com.furniro.OrderService.utils.error.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final KafkaProducer producer;
    private final PayPalService payPalService;

    // 1. Create order for user
    @Transactional
    public ResponseEntity<AType> createOrder(CreateOrderReq orderReq) {
        // Fetch user's cart from database
        Cart cart = cartRepository.findByUserID(orderReq.getUserID())
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_EXIST));

        List<CartItem> cartItems = cart.getItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new CartException(CartErrorCode.CART_ITEM_NOT_EXIST);
        }

        // Map request items to a price map: variantID -> price
        Map<Integer, BigDecimal> priceMap = orderReq.getOrderItems().stream()
                .filter(item -> item.getVariantID() != null && item.getPrice() != null)
                .collect(Collectors.toMap(OrderItemReq::getVariantID, OrderItemReq::getPrice, (p1, p2) -> p1));

        // Calculate total amount based on cart items and request prices
        BigDecimal totalAmount = cartItems.stream()
                .map(cartItem -> {
                    BigDecimal price = priceMap.get(cartItem.getVariantID());
                    if (price == null) {
                        throw new OrderException(OrderErrorCode.ORDER_ITEM_NOT_EXIST);
                    }
                    return price.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(BigDecimal.valueOf(orderReq.getShippingFee()));

        // Create and save order
        Order order = orderRepository.save(Order.builder()
                .userID(orderReq.getUserID())
                .orderNote(orderReq.getNote())
                .address(orderReq.getAddress())
                .shippingFee(BigDecimal.valueOf(orderReq.getShippingFee()))
                .totalAmount(totalAmount)
                .currency(orderReq.getCurrency())
                .status(OrderStatus.PENDING)
                .build());

        // Create and save order items from CartItems
        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            BigDecimal price = priceMap.get(cartItem.getVariantID());
            return OrderItem.builder()
                    .variant(cartItem.getVariantID())
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(price)
                    .order(order)
                    .build();
        }).toList();
        orderItemRepository.saveAll(orderItems);

        // Create payment template
        Payment payment = Payment.builder()
                .amount(order.getTotalAmount())
                .paymentMethod(orderReq.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .currency(orderReq.getCurrency())
                .order(order)
                .build();

        Object responseData = true;

        // Handle PayPal processing
        if (orderReq.getPaymentMethod() == PaymentMethod.PAYPAL) {
            Map<String, Object> paypalResponse = payPalService.createOrder(totalAmount.toPlainString(),
                    orderReq.getCurrency());
            payment.setPaypalOrderId((String) paypalResponse.get("id"));
            order.setStatus(OrderStatus.CREATED);

            // Extract the 'approve' link that navigates the user to the PayPal portal
            String approvalUrl = "";
            try {
                List<Map<String, String>> links = (List<Map<String, String>>) paypalResponse.get("links");
                if (links != null) {
                    approvalUrl = links.stream()
                            .filter(link -> "approve".equals(link.get("rel")))
                            .map(link -> link.get("href"))
                            .findFirst()
                            .orElse("");
                }
            } catch (Exception e) {
                log.warn("Could not extract PayPal approval link: {}", e.getMessage());
            }

            responseData = Map.of(
                    "paypalOrderId", paypalResponse.get("id"),
                    "approvalUrl", approvalUrl);
        }

        paymentRepository.save(payment);

        // Clear the user's cart (empty the cart after checkout)
        cartItems.clear();
        cartRepository.save(cart);

        // Kafka: notify order creation
        sendNotification(order.getUserID(), "Order Created", "Your order has been initiated successfully.");

        return ResponseEntity.ok(ApiType.success(responseData));
    }

    // 2. Capture PayPal order
    @Transactional
    @SuppressWarnings("unchecked")
    public ResponseEntity<AType> capturePayPalOrder(String paypalOrderId) {
        // Fetch current status from PayPal to make the operation idempotent
        Map<String, Object> orderDetails = payPalService.getOrderDetails(paypalOrderId);
        String orderStatus = (String) orderDetails.get("status");

        Map<String, Object> captureResponse;

        if ("COMPLETED".equals(orderStatus)) {

            log.info("PayPal order {} has already been successfully captured. Bypassing capture call.", paypalOrderId);
            captureResponse = orderDetails;

        } else {

            captureResponse = payPalService.captureOrder(paypalOrderId);

        }

        if (!"COMPLETED".equals(captureResponse.get("status"))) {

            return ResponseEntity.status(400).body(ErrorType.badRequest("Payment capture failed"));
        }

        Payment payment = paymentRepository.findByPaypalOrderId(paypalOrderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_EXIST));

        payment.setPaymentStatus(PaymentStatus.PAID);

        // Clean way to safely extract capture ID from nested Maps/Lists
        try {
            List<Map<String, Object>> purchaseUnits = (List<Map<String, Object>>) captureResponse.get("purchase_units");
            Map<String, Object> payments = (Map<String, Object>) purchaseUnits.get(0).get("payments");
            List<Map<String, Object>> captures = (List<Map<String, Object>>) payments.get("captures");
            payment.setPaypalCaptureId((String) captures.get(0).get("id"));
        } catch (Exception e) {
            log.warn("Could not extract PayPal capture ID for order ID: {}", paypalOrderId);
        }

        Order order = payment.getOrder();
        order.setStatus(OrderStatus.PAID);

        // Kafka: notify payment success
        sendNotification(order.getUserID(), "Order Paid", "Your payment was processed successfully!");

        return ResponseEntity.ok(ApiType.success(true));
    }

    // 3. Change order status for admin approval or cancellation
    @Transactional
    public ResponseEntity<AType> changeStatusOrder(UpdateStatusOrder updateStatusOrder) {
        Order order = orderRepository.findById(updateStatusOrder.getOrderID())
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_EXIST));

        order.setStatus(updateStatusOrder.getStatus());

        sendNotification(order.getUserID(), "Order " + updateStatusOrder.getStatus(),
                "Order updated to " + updateStatusOrder.getStatus() + " successfully.");
        return ResponseEntity.ok(ApiType.success(true));
    }

    // 4. KAFKA EVENT HANDLER (Triggered externally)
    @Transactional
    public void updateOrderStatus(Integer orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_EXIST));

        order.setStatus(OrderStatus.valueOf(status));

        sendNotification(order.getUserID(), "Order " + status, "Order updated to " + status + " successfully.");
        log.info("Order {} updated successfully via Kafka event", orderId);
    }

    // Shared Helper to keep Kafka notification blocks DRY (Don't Repeat Yourself)
    private void sendNotification(Integer userId, String title, String content) {
        Map<String, Object> data = Map.of(
                "userId", userId,
                "title", title,
                "content", content,
                "type", "UPDATE_ORDER_STATUS");
        producer.send("notification.created", data);
    }

    // 5. Admin: Get all orders with pagination and filtering
    public ResponseEntity<AType> getAllOrdersForAdmin(Integer page, Integer size, OrderStatus status, Integer userID) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderedAt").descending());
        Page<Order> orderPage;

        if (status != null && userID != null) {
            orderPage = orderRepository.findByStatusAndUserID(status, userID, pageable);
        } else if (status != null) {
            orderPage = orderRepository.findByStatus(status, pageable);
        } else if (userID != null) {
            orderPage = orderRepository.findByUserID(userID, pageable);
        } else {
            orderPage = orderRepository.findAll(pageable);
        }

        return ResponseEntity.ok(ApiType.success(orderPage));
    }

    // 6. Admin: Get specific order details
    public ResponseEntity<AType> getOrderDetailsForAdmin(Integer orderID) {
        Order order = orderRepository.findById(orderID)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_EXIST));
        return ResponseEntity.ok(ApiType.success(order));
    }
}