package services.interfaces;

import models.User;
import repositories.interfaces.IUserRepository;

public interface IAuthenticationService {
    boolean login(String username, String password, IUserRepository IUserRepository);
    void logout();
    User getAuthenticatedUser();
    boolean isAuthenticated();

}
