package services.implementations;

import models.User;
import repositories.interfaces.IUserRepository;
import services.interfaces.IAuthenticationService;

public class AuthenticationService implements IAuthenticationService {
    private User loggedInUser;

    public boolean login(String username, String password, IUserRepository IUserRepository) {
        User user = IUserRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            loggedInUser = user;
            return true;
        }
        return false;
    }

    public void logout() {
        loggedInUser = null;
    }

    public User getAuthenticatedUser() {
        return loggedInUser;
    }

    public boolean isAuthenticated() {
        return loggedInUser != null;
    }
}
