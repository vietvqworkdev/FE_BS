package services.implementations;

import enums.PaymentMethod;
import enums.PaymentStatus;
import models.*;
import repositories.interfaces.IBookRepository;
import repositories.interfaces.ICartRepository;
import repositories.interfaces.IOrderRepository;
import repositories.interfaces.IPaymentRepository;
import services.interfaces.IAuthenticationService;
import services.interfaces.IShoppingService;

import java.util.ArrayList;

public class ShoppingService implements IShoppingService {
    private final IBookRepository _bookRepository;
    private final ICartRepository _cartRepository;
    private final IOrderRepository _orderRepository;
    private final IAuthenticationService _authService;
    private final IPaymentRepository _paymentRepository;

    public ShoppingService(IBookRepository _bookRepository,
                           ICartRepository _cartRepository,
                           IOrderRepository _orderRepository,
                           IAuthenticationService _authService, IPaymentRepository paymentRepository) {
        this._bookRepository = _bookRepository;
        this._cartRepository = _cartRepository;
        this._orderRepository = _orderRepository;
        this._authService = _authService;
        _paymentRepository = paymentRepository;
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

    public Payment payOrder(Long orderId, PaymentMethod method) {
        if (!_authService.isAuthenticated()) {
            throw new IllegalStateException("User must login first");
        }
        User user = _authService.getAuthenticatedUser();

        Order order = _orderRepository.findById(orderId);
        if (order == null || !order.getUser().equals(user)) {
            throw new IllegalArgumentException("Order not found or not yours");
        }
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(method);
        payment.setAmount(order.getQuantity() * order.getBook().getPrice());
        payment.setStatus(PaymentStatus.COMPLETED);
        _paymentRepository.save(payment);
        return payment;
    }


    public Payment cancelPayment(Long paymentId) {
        if (!_authService.isAuthenticated()) {
            throw new IllegalStateException("User must login first");
        }
        User user = _authService.getAuthenticatedUser();

        Payment payment = _paymentRepository.findById(paymentId);
        if (payment == null || !payment.getOrder().getUser().equals(user)) {
            throw new IllegalArgumentException("Payment not found or not yours");
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a successful payment");
        }

        Book book = payment.getOrder().getBook();
        book.setStockQuantity(book.getStockQuantity() + payment.getOrder().getQuantity());
        _bookRepository.save(book);


        payment.setStatus(PaymentStatus.CANCELED);
        _paymentRepository.save(payment);

        return payment;
    }

    public Payment updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
        if (!_authService.isAuthenticated()) {
            throw new IllegalStateException("User must login first");
        }
        User user = _authService.getAuthenticatedUser();

        Payment payment = _paymentRepository.findById(paymentId);
        if (payment == null || !payment.getOrder().getUser().equals(user)) {
            throw new IllegalArgumentException("Payment not found or not yours");
        }

        if (payment.getStatus() == PaymentStatus.CANCELED) {
            throw new IllegalStateException("Cannot update a cancelled payment");
        }

        payment.setStatus(newStatus);
        _paymentRepository.save(payment);

        return payment;
    }


}
