package repositories.interfaces;

import models.Book;

public interface IBookRepository {
    Book findById(Long id);
    void save(Book book);
}


