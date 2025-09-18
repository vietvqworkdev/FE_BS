package services.interfaces;

import enums.PaymentMethod;
import enums.PaymentStatus;
import models.Payment;

public interface IShoppingService {
     void addToCart(long bookId, int quantity);
     void checkout();
    Payment payOrder(Long orderId, PaymentMethod method);
     Payment cancelPayment(Long paymentId);
    Payment updatePaymentStatus(Long paymentId, PaymentStatus newStatus);
}
