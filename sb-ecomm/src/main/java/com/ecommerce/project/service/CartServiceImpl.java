package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.CartItemDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        // 1. Find existing cart for this user if not present create a new cart.
        Cart userCart = fetchUserCart();
        // 2. Retrieve Product Details
        Product product = productRepository.findById(productId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Product","productId",productId)
                );
        // 3. Perform Validations
        // 3.1 check weather this product is present in that person cart
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(
                userCart.getCartId(),
                productId
        );
        // 3.2 if present throw exceptions
        if(cartItem != null){
            throw new APIException("Product "+ product.getProductName()+ " already exists");
        }
        // 3.3 if product quantity in the db is 0 then throw exception.
        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName() + " is not available");
        }
        // 3.4 if product quantity is less than requested throw an exception.
        if(product.getQuantity() < quantity){
            throw new APIException("Please , make an order of the "+product.getProductName() +
                    " less than or equal to the quantity "+ product.getQuantity() + "."
                    );
        }

        // 4. Create Cart Item
        CartItem newCartItem = new CartItem();

        newCartItem.setProduct(product);
        newCartItem.setCart(userCart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity());

        // 5. add item to the cart

        userCart.getCartItems().add(newCartItem);
        userCart.setTotalPrice(userCart.getTotalPrice() + (product.getSpecialPrice() * quantity));

        // 6. Save Cart Item
        cartRepository.save(userCart);

        // 6. Return updated cart
        CartDTO cartDTO = modelMapper.map(userCart,CartDTO.class);

        List<CartItem> userCartItems = userCart.getCartItems();

        Stream<ProductDTO> productStream = userCartItems.stream().map( item -> {
            ProductDTO map = modelMapper.map(item.getProduct(),ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        }
        );

        cartDTO.setProducts(productStream.toList());
        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if(carts.isEmpty()){
            throw new APIException("No cart exist");
        }

        List<CartDTO> cartDTOs = carts.stream()
                .map(
                        cart -> {
                            CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
                            List<ProductDTO> products = cart.getCartItems().stream()
                                    .map(
                                            cartItem -> {
                                               ProductDTO productDTO = modelMapper.map(cartItem.getProduct(),ProductDTO.class);
                                               productDTO.setQuantity(cartItem.getQuantity());
                                               return productDTO;
                                            }
                                    ).toList();
                            cartDTO.setProducts(products);
                            return cartDTO;
                        }
                ).collect(Collectors.toList());
        return cartDTOs;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId,cartId);
        if(cart == null){
            throw new ResourceNotFoundException("Cart","cartId",cartId);
        }
        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
        // quantity should be shown how many the person added
        // cartItem has the quantity column of how many items it has added.
        cart.getCartItems().forEach( c -> c.getProduct().setQuantity(c.getQuantity()));

        List<ProductDTO> products = cart.getCartItems().stream()
                .map(p->modelMapper.map(p.getProduct(),ProductDTO.class)).toList();
        cartDTO.setProducts(products);
        return cartDTO;
    }

    @Override
    @Transactional
    public CartDTO udpateProductQuantityInCart(Long productId, Integer quantity) {
        String emailId = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(emailId);
        Long cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow( ()-> new ResourceNotFoundException("Cart","cartId",cartId));

        // checking for the product in database.
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));

        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName() + " is not available");
        }

        if(product.getQuantity() < quantity){
            throw new APIException("Please , make an order of the "+product.getProductName() +
                    " less than or equal to the quantity "+ product.getQuantity() + "."
            );
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);

        if(cartItem == null){
            throw new APIException("Product "+product.getProductName()+" not available in the cart");
        }

        // calculate new quantity
        int newQuantity = cartItem.getQuantity() + quantity;

        // validation to prevent negative quantites
        if ( newQuantity < 0){
            throw new APIException("The resulting quantity cannot be negative!!!");
        }

        if( newQuantity == 0){
            deleteProductFromCart(cartId,productId);
        } else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(cart);
        }
        CartItem updatedItem = cartItemRepository.save(cartItem);
        if(updatedItem.getQuantity() == 0){
            cartItemRepository.deleteById(updatedItem.getCartItemId());
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO prd = modelMapper.map(item.getProduct(),ProductDTO.class);
            prd.setQuantity(item.getQuantity());
            return prd;
        });

        cartDTO.setProducts(productStream.toList());
        return cartDTO;
    }

    @Override
    @Transactional
    public String deleteProductFromCart(Long cartId, Long productId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Cart","cartId",cartId)
                );
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);

        if(cartItem == null){
            throw new ResourceNotFoundException("Product","productId",productId);
        }

        cart.setTotalPrice(
                cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity())
        );

        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId,productId);

        return "Product "+ cartItem.getProduct().getProductName() + " has been deleted successfully.";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow( ()-> new ResourceNotFoundException("Cart","cartId",cartId));

        // checking for the product in database.
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);

        if(cartItem == null){
            throw new APIException("Product "+product.getProductName()+ " not available.");
        }

        // 1000 - 100*2 = 800
        // getting cart value after removing old product price
        double cartPrice = cart.getTotalPrice() -
                (cartItem.getProductPrice() * cartItem.getQuantity());

        // setting the new product price. - 200
        cartItem.setProductPrice(product.getSpecialPrice());

        // adding new product price to cart
        // 800 + 200*2 = 1200
        cart.setTotalPrice( cartPrice +
                (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItem = cartItemRepository.save(cartItem);
    }

    @Transactional
    @Override
    public String createOrUpdateCartWithItems(List<CartItemDTO> cartItems) {

        // Get User's email
        String emailId = authUtil.loggedInEmail();

        // Check if an existing cart is available or create a new one
        Cart existingCart = cartRepository.findCartByEmail(emailId);

        if(existingCart == null){
            existingCart = new Cart();
            existingCart.setTotalPrice(0.00);
            existingCart.setUser(authUtil.loggedInUser());
            existingCart = cartRepository.save(existingCart);
        }
        else {
            // Clear all current items in the existing cart
            cartItemRepository.deleteAllByCartId((existingCart.getCartId()));
        }
        double totalPrice = 0;
        // process each item in the request to add to the cart
        for(CartItemDTO cartItemDTO: cartItems){
            Long productId = cartItemDTO.getProductId();
            Integer quantity = cartItemDTO.getQuantity();
            // Find the product by ID
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product","productId",productId));
            // Directly update product stock and total price
            // updated the product stock.
            // 1. when the order is placed. (mostly optimal)
            // 2. when product is added to cart
            // product.setQuantity(product.getQuantity() - quantity);
            totalPrice += product.getSpecialPrice() * quantity;
            // Create and save cart item
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCart(existingCart);
            cartItem.setQuantity(quantity);
            cartItem.setProductPrice((product.getSpecialPrice()));
            cartItemRepository.save(cartItem);
        }
        // Update the cart's total price and save

        existingCart.setTotalPrice(totalPrice);

        cartRepository.save(existingCart);
        return "Cart created/updated with the new items successfully.";
    }

    private Cart fetchUserCart(){
        // 1. find the user cart by email ? return the cart : create new one.
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());

        if(userCart!=null){
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        cart.setUser(authUtil.loggedInUser());
        return cartRepository.save(cart);
    }
}
