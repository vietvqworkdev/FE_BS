package models;

import enums.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String address;
    private String phone;
    private String role;

    public User(String viet, String password) {
        this.username = viet;
        this.password = password;
    }
}














