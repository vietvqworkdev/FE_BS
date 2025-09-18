package repositories.interfaces;


import models.Order;

public interface IOrderRepository {
    void save(Order order);

    Order findById(Long orderId);
}
