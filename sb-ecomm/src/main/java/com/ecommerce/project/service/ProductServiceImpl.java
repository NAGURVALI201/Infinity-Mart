package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import com.ecommerce.project.model.Category;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;


import java.io.IOException;
import java.util.List;


@Service
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final FileService fileService;
    @Value("${project.image}")
    private String path;
    private CartRepository cartRepository;
    private CartService cartService;
    @Value("${image.base.url}")
    private String imageBaseUrl;
    private final AuthUtil authUtil;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ModelMapper modelMapper,
                              FileService fileService,
                              CartRepository cartRepository,
                              CartService cartService, AuthUtil authUtil){
        this.productRepository =productRepository;
        this.categoryRepository= categoryRepository;
        this.modelMapper = modelMapper;
        this.fileService = fileService;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.authUtil = authUtil;
    }

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow( ()-> new ResourceNotFoundException("Category","categoryId",categoryId));

        boolean isProductNotPresent = true;

        List<Product> products = category.getProducts();

        for(Product value:products){
            if(value.getProductName().equals(productDTO.getProductName())){
                isProductNotPresent = false;
                break;
            }
        }

        if(isProductNotPresent)
        {
            Product product = modelMapper.map(productDTO,Product.class);
            product.setImage("default.png");
            product.setCategory(category);
            product.setUser(authUtil.loggedInUser());
            double specialPrice = product.getPrice() -
                    (product.getPrice() * product.getDiscount() * 0.01);
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);

            return modelMapper.map(savedProduct, ProductDTO.class);
        }
        else {
            throw new APIException("Product already exists!!!");
        }

    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortDir, String keyword, String category) {

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);

        Specification<Product> spec = Specification.where(null);

        if(keyword != null && !keyword.isEmpty()){
            spec = spec.and((root,query,criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")),"%"+keyword.toLowerCase()+"%")
            );
        }

        if(category != null && !category.isEmpty()){
            spec = spec.and((root,query,criteriaBuilder) ->
                    criteriaBuilder.like(root.get("category").get("categoryName"),category)
            );
        }

        Page<Product> pageProducts = productRepository.findAll(spec,pageDetails);

        List<Product> products = pageProducts.getContent();

//        if(products.isEmpty()){
//            throw new APIException("No products found!!!");
//        }

        List<ProductDTO> productDTOS = products.stream()
                .map( product -> {
                        ProductDTO productDto = modelMapper.map(product,ProductDTO.class);
                        productDto.setImage(constructImageUrl(product.getImage()));
                        return productDto;
                })
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setLastPage(pageProducts.isLast());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setPageSize(pageProducts.getSize());

        return productResponse;
    }

    public String constructImageUrl(String image){
        return imageBaseUrl.endsWith("/") ? imageBaseUrl + image : imageBaseUrl + "/" + image;
    }
    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","categoryId",categoryId));

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);

        Page<Product> pageProducts = productRepository.findByCategoryOrderByPriceAsc(category,pageDetails);

        List<Product> products = pageProducts.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map( product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        if(productDTOS.isEmpty()){
            throw new APIException("product found in this category: "+category.getCategoryName());
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setLastPage(pageProducts.isLast());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setPageSize(pageProducts.getSize());

        return productResponse;
    }

    @Override
    public ProductResponse getProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);

        Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase("%"+keyword+"%",pageDetails);

        List<Product> products = pageProducts.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        if(productDTOS.isEmpty()){
            throw new APIException("No products found with this keyword:"+keyword);
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setLastPage(pageProducts.isLast());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setPageSize(pageProducts.getSize());
        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product productDB = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));

        Product product = modelMapper.map(productDTO,Product.class);

        productDB.setProductName(product.getProductName());
        productDB.setDescription(product.getDescription());
        productDB.setQuantity(product.getQuantity());
        productDB.setPrice(product.getPrice());
        productDB.setDiscount(product.getDiscount());
        productDB.setSpecialPrice(product.getSpecialPrice());

        Product savedProduct = productRepository.save(productDB);

        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        List<CartDTO> cartDTOs = carts.stream().map(
                cart -> {
                    CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
                    List<ProductDTO> products = cart.getCartItems().stream()
                            .map(
                                    p-> modelMapper.map(p.getProduct(),ProductDTO.class)
                            ).toList();
                    cartDTO.setProducts(products);
                    return cartDTO;
                }
        ).toList();

        cartDTOs.forEach(
                cart -> cartService.updateProductInCarts(cart.getCartId(),productId)
        );
        return modelMapper.map(savedProduct,ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));

        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(
                cart -> cartService.deleteProductFromCart(cart.getCartId(),productId)
        );
        productRepository.delete(product);
        return modelMapper.map(product,ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        // Get the product from DB
        Product productFromDB = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));

        // Upload image to server
        // Get the filename of uploaded image

        String fileName = fileService.uploadImage(path,image);

        // Updating the new file name to the product
        productFromDB.setImage(fileName);

        // save updated product
        Product updatedProduct = productRepository.save(productFromDB);

        // return DTO after mapping product to DTO

        return modelMapper.map(updatedProduct,ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProductsForAdmin(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);

        Page<Product> pageProducts = productRepository.findAll(pageDetails);

        List<Product> products = pageProducts.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map( product -> {
                    ProductDTO productDto = modelMapper.map(product,ProductDTO.class);
                    productDto.setImage(constructImageUrl(product.getImage()));
                    return productDto;
                })
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setLastPage(pageProducts.isLast());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setPageSize(pageProducts.getSize());

        return productResponse;
    }

    @Override
    public ProductResponse getAllProductsForSeller(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        User user = authUtil.loggedInUser();
        Page<Product> pageProducts = productRepository.findByUser(user,pageDetails);

        List<Product> products = pageProducts.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map( product -> {
                    ProductDTO productDto = modelMapper.map(product,ProductDTO.class);
                    productDto.setImage(constructImageUrl(product.getImage()));
                    return productDto;
                })
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setLastPage(pageProducts.isLast());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setPageSize(pageProducts.getSize());

        return productResponse;
    }
}
