package repositories.interfaces;

import models.Cart;
import models.User;

public interface ICartRepository {
    Cart findByUser(User user);
    void save(Cart cart);
}