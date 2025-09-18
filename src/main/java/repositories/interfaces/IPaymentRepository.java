package repositories.interfaces;


import models.Payment;

public interface IPaymentRepository {
    void save(Payment payment);
    Payment findById(Long id);
}