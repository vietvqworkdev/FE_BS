package models;

import enums.PaymentMethod;
import enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Payment {
    private Long id;
    private Order order;
    private PaymentMethod method;
    private double amount;
    private PaymentStatus status;
}