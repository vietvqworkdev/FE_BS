package services;

import models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repositories.interfaces.IUserRepository;
import services.implementations.AuthenticationService;
import services.interfaces.IAuthenticationService;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AuthenticationServiceTest {
    private IUserRepository _userRepo;
    private IAuthenticationService _authService;

    @BeforeEach
    void setup() {
        _userRepo = mock(IUserRepository.class);
        _authService = new AuthenticationService();
    }

    @Test
    void login_given_correct_credentials_when_login_then_success() {
        User user = new User("viet", "123");
        when(_userRepo.findByUsername("viet")).thenReturn(user);
        boolean result = _authService.login("viet", "123", _userRepo);
        assertThat(result, is(true));
        assertThat(_authService.isAuthenticated(), is(true));
        assertThat(_authService.getAuthenticatedUser(), equalTo(user));
    }

    @Test
    void login_given_wrong_password_when_login_then_fail() {
        User user = new User("viet", "123");
        when(_userRepo.findByUsername("viet")).thenReturn(user);
        boolean result = _authService.login("viet", "wrong", _userRepo);
        assertThat(result, is(false));
        assertThat(_authService.isAuthenticated(), is(false));
    }

    @Test
    void login_given_user_not_found_when_login_then_fail() {
        when(_userRepo.findByUsername("viet")).thenReturn(null);
        boolean result = _authService.login("viet", "123", _userRepo);
        assertThat(result, is(false));
        assertThat(_authService.isAuthenticated(), is(false));
    }

    @Test
    void logout_given_logged_in_user_when_logout_then_clear_session() {
        User user = new User("viet", "123");
        when(_userRepo.findByUsername("viet")).thenReturn(user);
        _authService.login("viet", "123", _userRepo);
        _authService.logout();
        assertThat(_authService.isAuthenticated(), is(false));
        assertThat(_authService.getAuthenticatedUser(), nullValue());
    }
}
