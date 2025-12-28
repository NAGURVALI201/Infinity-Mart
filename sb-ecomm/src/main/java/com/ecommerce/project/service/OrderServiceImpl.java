package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.payload.OrderResponse;
import com.ecommerce.project.repositories.*;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService{

    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;
    private final AuthUtil authUtil;


    @Autowired
    public OrderServiceImpl(CartRepository cartRepository,
                            AddressRepository addressRepository,
                            OrderRepository orderRepository,
                            PaymentRepository paymentRepository,
                            OrderItemRepository orderItemRepository,
                            ProductRepository productRepository,
                            CartService cartService,
                            ModelMapper modelMapper, AuthUtil authUtil
    ) {
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.modelMapper = modelMapper;
        this.authUtil = authUtil;
    }

    @Override
    @Transactional
    public OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
        // Getting the user cart
        Cart cart = cartRepository.findCartByEmail(emailId);
        if(cart == null){
            throw new ResourceNotFoundException("Cart","email",emailId);
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Address","addressId",addressId)
                );
        // Creating a new order with payment info
        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setAddress(address);
        order.setOrderStatus("Accepted");
        order.setTotalAmount(cart.getTotalPrice());

        Payment payment = new Payment(
                paymentMethod,
                pgPaymentId,
                pgStatus,
                pgResponseMessage,
                pgName
                );
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);
        // Get items into the cart into order items
        List<CartItem> cartItems = cart.getCartItems();
        if(cartItems.isEmpty()){
            throw new APIException("Cart is Empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();

        for(CartItem cartItem: cartItems){
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);
        // Update product stock
        cart.getCartItems().forEach(
                item -> {
                    int quantity = item.getQuantity();
                    Product product = item.getProduct();
                    product.setQuantity(product.getQuantity() - quantity);
                    productRepository.save(product);

                    // Clear the cart
                    cartService.deleteProductFromCart(cart.getCartId(),product.getProductId());
                }
        );

        OrderDTO orderDTO = modelMapper.map(savedOrder,OrderDTO.class);
        orderDTO.setAddressId(addressId);

        // we will add all the orderItemDTO to orderDTO
        orderItems.forEach(
                orderItem -> orderDTO
                        .getOrderItems()
                        .add(modelMapper.map(orderItem, OrderItemDTO.class))
        );
        // Send back the order summary

        return orderDTO;


    }

    @Override
    public OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Order> pageOrders = orderRepository.findAll(pageDetails);
        List<Order> orders = pageOrders.getContent();

        List<OrderDTO> orderDTOs = orders.stream()
                .map(order -> modelMapper.map(order,OrderDTO.class))
                .toList();

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setContent(orderDTOs);
        orderResponse.setPageNumber(pageOrders.getNumber());
        orderResponse.setLastPage(pageOrders.isLast());
        orderResponse.setPageSize(pageOrders.getSize());
        orderResponse.setTotalPages(pageOrders.getTotalPages());
        orderResponse.setTotalElements(pageOrders.getTotalElements());

        return orderResponse;
    }

    @Override
    public OrderDTO updateOrder( Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new ResourceNotFoundException("Order","orderId",orderId));
        order.setOrderStatus(status);
        order = orderRepository.save(order);
        return modelMapper.map(order,OrderDTO.class);
    }

    @Override
    public OrderResponse getAllSellerOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);

        User seller = authUtil.loggedInUser();

        Page<Order> pageOrders = orderRepository.findAll(pageDetails);
        List<Order> sellerOrders = pageOrders.getContent()
                .stream()
                .filter(order ->
                                order
                                .getOrderItems()
                                .stream()
                                .anyMatch(
                                        orderItem -> {
                                            var product = orderItem.getProduct();
                                            if(product == null || product.getUser() == null) {
                                                return false;
                                            }
                                            return product
                                                .getUser()
                                                .getUserId()
                                                .equals(seller.getUserId());
                                        }
                                )

                ).toList();

        List<OrderDTO> orderDTOs = sellerOrders.stream()
                .map(order -> modelMapper.map(order,OrderDTO.class))
                .toList();

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setContent(orderDTOs);
        orderResponse.setPageNumber(pageOrders.getNumber());
        orderResponse.setLastPage(pageOrders.isLast());
        orderResponse.setPageSize(pageOrders.getSize());
        orderResponse.setTotalPages(pageOrders.getTotalPages());
        orderResponse.setTotalElements(pageOrders.getTotalElements());


        return orderResponse;
    }
}
