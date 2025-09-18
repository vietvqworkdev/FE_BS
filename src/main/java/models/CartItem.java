package models;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItem {
    private Long id;
    private Book book;
    private int quantity;
}
