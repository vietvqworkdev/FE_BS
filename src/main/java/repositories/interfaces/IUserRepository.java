package repositories.interfaces;

import models.User;

public interface IUserRepository {
    User findByUsername(String username);
}
