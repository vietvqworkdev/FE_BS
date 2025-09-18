package services.interfaces;

public interface IShoppingService {
     void addToCart(long bookId, int quantity);
     void checkout();
}
