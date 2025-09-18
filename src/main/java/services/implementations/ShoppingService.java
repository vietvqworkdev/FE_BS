package services.implementations;

import models.*;
import repositories.interfaces.IBookRepository;
import repositories.interfaces.ICartRepository;
import repositories.interfaces.IOrderRepository;
import services.interfaces.IAuthenticationService;
import services.interfaces.IShoppingService;

import java.util.ArrayList;

public class ShoppingService implements IShoppingService {
    private final IBookRepository _bookRepository;
    private final ICartRepository _cartRepository;
    private final IOrderRepository _orderRepository;
    private final IAuthenticationService _authService;

    public ShoppingService(IBookRepository _bookRepository,
                           ICartRepository _cartRepository,
                           IOrderRepository _orderRepository,
                           IAuthenticationService _authService) {
        this._bookRepository = _bookRepository;
        this._cartRepository = _cartRepository;
        this._orderRepository = _orderRepository;
        this._authService = _authService;
    }

    public void addToCart(long bookId, int quantity) {
        if (!_authService.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        User user = _authService.getAuthenticatedUser();
        Book book = _bookRepository.findById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book not found");
        }
        int allowedQuantity = Math.min(quantity, book.getStockQuantity());
        Cart cart = _cartRepository.findByUser(user);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setItems(new ArrayList<>());
        }

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }
        CartItem existing = cart.getItems().stream()
                .filter(ci -> ci.getBook().getId().equals(bookId))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            int newQty = Math.min(existing.getQuantity() + allowedQuantity, book.getStockQuantity());
            existing.setQuantity(newQty);
        } else {
            CartItem item = new CartItem();
            item.setBook(book);
            item.setQuantity(allowedQuantity);
            cart.getItems().add(item);
        }

        _cartRepository.save(cart);
    }


    public void checkout() {
        if (!_authService.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        User user = _authService.getAuthenticatedUser();

        Cart cart = _cartRepository.findByUser(user);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        for (CartItem item : cart.getItems()) {
            Book book = item.getBook();
            if (book.getStockQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Not enough stock for " + book.getTitle());
            }
            book.setStockQuantity(book.getStockQuantity() - item.getQuantity());
            _bookRepository.save(book);

            Order order = new Order();
            order.setBook(book);
            order.setQuantity(item.getQuantity());
            _orderRepository.save(order);
        }

        cart.getItems().clear();
        _cartRepository.save(cart);
    }
}
