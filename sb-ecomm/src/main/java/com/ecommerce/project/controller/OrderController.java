package com.ecommerce.project.controller;


import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.security.services.UserDetailsImpl;
import com.ecommerce.project.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.project.util.AuthUtil;
import com.ecommerce.project.service.OrderService;

@RestController
@RequestMapping("/api")
public class OrderController {

    private OrderService orderService;

    private AuthUtil authUtil;

    private StripeService stripeService;

    @Autowired
    public OrderController(OrderService orderService, AuthUtil authUtil, StripeService stripeService) {
        this.orderService = orderService;
        this.authUtil = authUtil;
        this.stripeService = stripeService;
    }

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> createOrder(
            @PathVariable String paymentMethod,
            @RequestBody OrderRequestDTO orderRequestDTO
            ){
        String emailId = authUtil.loggedInEmail();
        OrderDTO orderDTO = orderService.placeOrder(
                emailId,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );

        return new ResponseEntity<>(orderDTO, HttpStatus.CREATED);
    }

    @PostMapping("/order/stripe-client-secret")
    public ResponseEntity<String> createPayment(@RequestBody StripePaymentDTO stripePaymentDTO) throws StripeException {

        PaymentIntent paymentIntent = stripeService.paymentIntent(stripePaymentDTO);

        return new ResponseEntity<>(paymentIntent.getClientSecret(),HttpStatus.CREATED);
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<OrderResponse> getAllOrders(
            @RequestParam(name="pageNumber",defaultValue= AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
            @RequestParam(name="pageSize", defaultValue=AppConstants.PAGE_SIZE,required = false) Integer pageSize,
            @RequestParam(name="sortBy", defaultValue = AppConstants.SORT_ORDERS_BY,required = false) String sortBy,
            @RequestParam(name="sortOrder", defaultValue = AppConstants.SORT_DIR,required = false) String sortDir
    ){
        OrderResponse response = orderService.getAllOrders(pageNumber,pageSize,sortBy,sortDir);
        return new ResponseEntity<OrderResponse>(response,HttpStatus.OK);

    }

    @GetMapping("/seller/orders")
    public ResponseEntity<OrderResponse> getAllSellerOrders(
            @RequestParam(name="pageNumber",defaultValue= AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
            @RequestParam(name="pageSize", defaultValue=AppConstants.PAGE_SIZE,required = false) Integer pageSize,
            @RequestParam(name="sortBy", defaultValue = AppConstants.SORT_ORDERS_BY,required = false) String sortBy,
            @RequestParam(name="sortOrder", defaultValue = AppConstants.SORT_DIR,required = false) String sortDir
    ){
        OrderResponse response = orderService.getAllSellerOrders(pageNumber,pageSize,sortBy,sortDir);
        return new ResponseEntity<OrderResponse>(response,HttpStatus.OK);
    }

    @PutMapping("/admin/orders/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateDTO orderStatusUpdateDTO
    ){
        OrderDTO order = orderService.updateOrder(orderId,orderStatusUpdateDTO.getStatus());
        return new ResponseEntity<>(order,HttpStatus.OK);
    }

    @PutMapping("/seller/orders/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatusSeller(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateDTO orderStatusUpdateDTO
    ){
        OrderDTO order = orderService.updateOrder(orderId,orderStatusUpdateDTO.getStatus());
        return new ResponseEntity<>(order,HttpStatus.OK);
    }
}
