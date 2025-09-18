package models;


import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
public class Cart {
    private Long id;
    private User user;
    private List<CartItem> items = new ArrayList<>();


}
