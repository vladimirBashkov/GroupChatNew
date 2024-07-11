package main.security;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import main.entity.Status;
import main.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@RequiredArgsConstructor
public class SecurityUser implements UserDetails {

    private final User user;

    public Long getId(){
        return user.getId();
    }

    public String getLogin(){
        return user.getLogin();
    }

    public String getEmail(){
        return user.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getLogin();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.getStatus().equals(Status.ACTIVE);
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.getStatus().equals(Status.BANNED);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.getStatus().equals(Status.ACTIVE);
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus().equals(Status.ACTIVE);
    }

}
