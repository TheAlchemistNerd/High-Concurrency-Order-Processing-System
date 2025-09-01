package com.ecommerce.orderprocessing.service;

import com.ecommerce.orderprocessing.domain.entity.Customer;
import com.ecommerce.orderprocessing.domain.entity.Product;
import com.ecommerce.orderprocessing.domain.entity.ShoppingCart;
import com.ecommerce.orderprocessing.domain.entity.ShoppingCartItem;
import com.ecommerce.orderprocessing.dto.request.AddItemToCartRequest;
import com.ecommerce.orderprocessing.dto.request.UpdateCartItemQuantityRequest;
import com.ecommerce.orderprocessing.dto.response.ShoppingCartDto;
import com.ecommerce.orderprocessing.dto.response.ShoppingCartItemDto;
import com.ecommerce.orderprocessing.exception.ResourceNotFoundException;
import com.ecommerce.orderprocessing.repository.CustomerRepository;
import com.ecommerce.orderprocessing.repository.ProductRepository;
import com.ecommerce.orderprocessing.repository.ShoppingCartItemRepository;
import com.ecommerce.orderprocessing.repository.ShoppingCartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ExecutorService virtualThreadExecutor;

    public ShoppingCartServiceImpl(ShoppingCartRepository shoppingCartRepository, ShoppingCartItemRepository shoppingCartItemRepository,
                                   CustomerRepository customerRepository, ProductRepository productRepository,
                                   ExecutorService virtualThreadExecutor) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.shoppingCartItemRepository = shoppingCartItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    @Override
    public CompletableFuture<ShoppingCartDto> getShoppingCart(Long customerId) {
        return CompletableFuture.supplyAsync(() -> {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

            ShoppingCart cart = shoppingCartRepository.findByCustomer(customer)
                    .orElseGet(() -> createNewShoppingCart(customer));

            return toShoppingCartDto(cart);
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<ShoppingCartDto> addItemToCart(Long customerId, AddItemToCartRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

            ShoppingCart cart = shoppingCartRepository.findByCustomer(customer)
                    .orElseGet(() -> createNewShoppingCart(customer));

            Product product = productRepository.findById(request.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            // Check if item already exists in cart
            cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(product.getId()))
                    .findFirst()
                    .ifPresentOrElse(
                            item -> {
                                item.setQuantity(item.getQuantity() + request.quantity());
                                shoppingCartItemRepository.save(item);
                            },
                            () -> {
                                ShoppingCartItem newItem = new ShoppingCartItem();
                                newItem.setShoppingCart(cart);
                                newItem.setProduct(product);
                                newItem.setQuantity(request.quantity());
                                newItem.setUnitPrice(product.getPrice());
                                shoppingCartItemRepository.save(newItem);
                                cart.getItems().add(newItem);
                            }
                    );

            return toShoppingCartDto(cart);
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<ShoppingCartDto> updateItemQuantity(Long customerId, Long productId, UpdateCartItemQuantityRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

            ShoppingCart cart = shoppingCartRepository.findByCustomer(customer)
                    .orElseThrow(() -> new ResourceNotFoundException("Shopping cart not found"));

            ShoppingCartItem itemToUpdate = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

            itemToUpdate.setQuantity(request.quantity());
            shoppingCartItemRepository.save(itemToUpdate);

            return toShoppingCartDto(cart);
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<ShoppingCartDto> removeItemFromCart(Long customerId, Long productId) {
        return CompletableFuture.supplyAsync(() -> {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

            ShoppingCart cart = shoppingCartRepository.findByCustomer(customer)
                    .orElseThrow(() -> new ResourceNotFoundException("Shopping cart not found"));

            ShoppingCartItem itemToRemove = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

            cart.getItems().remove(itemToRemove);
            shoppingCartItemRepository.delete(itemToRemove);

            return toShoppingCartDto(cart);
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<Void> clearShoppingCart(Long customerId) {
        return CompletableFuture.runAsync(() -> {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

            ShoppingCart cart = shoppingCartRepository.findByCustomer(customer)
                    .orElseThrow(() -> new ResourceNotFoundException("Shopping cart not found"));

            shoppingCartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
            shoppingCartRepository.save(cart);
        }, virtualThreadExecutor);
    }

    private ShoppingCart createNewShoppingCart(Customer customer) {
        ShoppingCart newCart = new ShoppingCart();
        newCart.setCustomer(customer);
        return shoppingCartRepository.save(newCart);
    }

    private ShoppingCartDto toShoppingCartDto(ShoppingCart cart) {
        List<ShoppingCartItemDto> itemDtos = cart.getItems().stream()
                .map(this::toShoppingCartItemDto)
                .collect(Collectors.toList());

        BigDecimal totalAmount = itemDtos.stream()
                .map(ShoppingCartItemDto::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ShoppingCartDto(
                cart.getId(),
                cart.getCustomer().getId(),
                itemDtos,
                totalAmount
        );
    }

    private ShoppingCartItemDto toShoppingCartItemDto(ShoppingCartItem item) {
        return new ShoppingCartItemDto(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
        );
    }
}
