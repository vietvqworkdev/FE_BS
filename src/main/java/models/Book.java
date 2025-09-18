package models;

import lombok.*;

@Getter
@Setter
public class Book {
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private double price;
    private int stockQuantity;
    private String description;
    private Category category;


}
