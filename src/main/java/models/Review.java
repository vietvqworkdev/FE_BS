package models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Review {
    private Long id;
    private User user;
    private Book book;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}