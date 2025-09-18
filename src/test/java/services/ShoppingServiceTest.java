package _shopingServices;

import models.*;
import models.Order;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;
import repositories.interfaces.IBookRepository;
import repositories.interfaces.ICartRepository;
import repositories.interfaces.IOrderRepository;
import services.implementations.ShoppingService;
import services.interfaces.IAuthenticationService;
import services.interfaces.IShoppingService;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ShoppingServiceTest {
    private IBookRepository _bookRepo;
    private ICartRepository _cartRepo;
    private IOrderRepository _orderRepo;
    private IAuthenticationService _authService;
    private IShoppingService _shoppingService;
    private User user;

    @BeforeEach
    void setup() {
        _bookRepo = mock(IBookRepository.class);
        _cartRepo = mock(ICartRepository.class);
        _orderRepo = mock(IOrderRepository.class);
        _authService = mock(IAuthenticationService.class);
        _shoppingService = new ShoppingService(_bookRepo, _cartRepo, _orderRepo, _authService);
        user = new User();
        user.setId(1L);
        user.setUsername("viet");
        user.setPassword("123");
        when(_authService.isAuthenticated()).thenReturn(true);
        when(_authService.getAuthenticatedUser()).thenReturn(user);
    }

    @Tag("addToCart")
    @org.junit.jupiter.api.Order(1)
    @Test
    void addToCart_given_quantity_exceeds_stock_when_adding_then_limit_by_stock() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Book 1");
        book.setStockQuantity(2);
        when(_bookRepo.findById(1L)).thenReturn(book);
        when(_cartRepo.findByUser(user)).thenReturn(new Cart());
        _shoppingService.addToCart(1L, 5);
        verify(_cartRepo).save(any());
        assertThat(book.getStockQuantity(), equalTo(2));
    }

    @Tag("checkout")
    @org.junit.jupiter.api.Order(2)
    @Test
    void checkout_given_cart_with_items_when_checkout_then_decrease_stock_and_create_order() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Book 1");
        book.setStockQuantity(10);
        Cart cart = new Cart();
        cart.setUser(user);
        CartItem item = new CartItem();
        item.setBook(book);
        item.setQuantity(3);
        cart.getItems().add(item);
        when(_cartRepo.findByUser(user)).thenReturn(cart);
        _shoppingService.checkout();
        verify(_bookRepo).save(book);
        verify(_orderRepo).save(any());
        verify(_cartRepo, atLeastOnce()).save(cart);
        assertThat(book.getStockQuantity(), equalTo(7));
        assertThat(cart.getItems(), empty());
    }

    @Tag("checkout")
    @org.junit.jupiter.api.Order(3)
    @Test
    void checkout_given_empty_cart_when_checkout_then_throw_exception() {
        Cart cart = new Cart();
        cart.setUser(user);
        when(_cartRepo.findByUser(user)).thenReturn(cart);
        Exception ex = assertThrows(IllegalStateException.class,
                () -> _shoppingService.checkout());
        assertThat(ex.getMessage(), containsString("Cart is empty"));
    }

    @Tag("addToCart")
    @org.junit.jupiter.api.Order(4)
    @Test
    void addToCart_given_no_cart_when_adding_then_create_new_cart() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Book 1");
        book.setStockQuantity(5);
        when(_bookRepo.findById(1L)).thenReturn(book);
        when(_cartRepo.findByUser(user)).thenReturn(null);
        _shoppingService.addToCart(1L, 2);
        verify(_cartRepo).save(ArgumentMatchers.any(Cart.class));
    }

    @Tag("addToCart")
    @org.junit.jupiter.api.Order(5)
    @Test
    void addToCart_given_book_already_in_cart_when_adding_then_increase_quantity() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Book 2");
        book.setStockQuantity(10);
        Cart cart = new Cart();
        cart.setUser(user);
        CartItem item = new CartItem();
        item.setBook(book);
        item.setQuantity(2);
        cart.getItems().add(item);
        when(_bookRepo.findById(1L)).thenReturn(book);
        when(_cartRepo.findByUser(user)).thenReturn(cart);
        _shoppingService.addToCart(1L, 3);
        assertThat(cart.getItems(), hasSize(1));
        assertThat(cart.getItems().get(0).getQuantity(), equalTo(5));
    }

    @Tag("addToCart")
    @org.junit.jupiter.api.Order(6)
    @Test
    void addToCart_given_book_not_found_when_adding_then_throw_exception() {
        when(_bookRepo.findById(99L)).thenReturn(null);
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> _shoppingService.addToCart(99L, 1));
        assertThat(ex.getMessage(), containsString("Book not found"));
    }

    @Tag("checkout")
    @org.junit.jupiter.api.Order(7)
    @Test
    void checkout_given_stock_not_enough_when_checkout_then_throw_exception() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("DDD");
        book.setStockQuantity(2);
        Cart cart = new Cart();
        cart.setUser(user);
        CartItem item = new CartItem();
        item.setBook(book);
        item.setQuantity(5);
        cart.getItems().add(item);
        when(_cartRepo.findByUser(user)).thenReturn(cart);
        Exception ex = assertThrows(IllegalStateException.class,
                () -> _shoppingService.checkout());

        assertThat(ex.getMessage(), containsString("Not enough stock"));
    }
    @Tag("checkout")

    @org.junit.jupiter.api.Order(8)
    @Test
    void checkout_given_multiple_items_when_checkout_then_save_each_order() {
        Book b1 = new Book();
        b1.setId(1L);
        b1.setTitle("Book A");
        b1.setStockQuantity(5);
        Book b2 = new Book();
        b2.setId(2L);
        b2.setTitle("Book B");
        b2.setStockQuantity(10);
        Cart cart = new Cart();
        cart.setUser(user);
        CartItem i1 = new CartItem();
        i1.setBook(b1);
        i1.setQuantity(2);
        CartItem i2 = new CartItem();
        i2.setBook(b2);
        i2.setQuantity(3);
        cart.getItems().add(i1);
        cart.getItems().add(i2);
        when(_cartRepo.findByUser(user)).thenReturn(cart);
        _shoppingService.checkout();
        verify(_bookRepo).save(b1);
        verify(_bookRepo).save(b2);
        verify(_orderRepo, times(2)).save(ArgumentMatchers.any(Order.class));
        assertThat(cart.getItems(), empty());
    }

    @Tag("addToCart")
    @org.junit.jupiter.api.Order(9)
    @Test
    void addToCart_given_quantity_zero_when_adding_then_ignore_or_exception() {
        Book book = new Book();
        book.setId(1L);
        book.setStockQuantity(10);
        when(_bookRepo.findById(1L)).thenReturn(book);
        when(_cartRepo.findByUser(user)).thenReturn(new Cart());
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> _shoppingService.addToCart(1L, 0));
        assertThat(ex.getMessage(), containsString("Quantity must be greater than 0"));
    }

    @Tag("unauthenticated")
    @org.junit.jupiter.api.Order(10)
    @Test
    void addToCart_given_not_authenticated_when_adding_then_throw_exception() {
        when(_authService.isAuthenticated()).thenReturn(false);
        Exception ex = assertThrows(SecurityException.class,
                () -> _shoppingService.addToCart(1L, 1));
        assertThat(ex.getMessage(), containsString("User not authenticated"));
    }

    @Tag("unauthenticated")
    @org.junit.jupiter.api.Order(11)
    @Test
    void checkout_given_not_authenticated_when_checkout_then_throw_exception() {
        when(_authService.isAuthenticated()).thenReturn(false);
        Exception ex = assertThrows(SecurityException.class,
                () -> _shoppingService.checkout());
        assertThat(ex.getMessage(), containsString("User not authenticated"));
    }

}
