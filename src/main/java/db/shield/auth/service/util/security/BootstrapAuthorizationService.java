package db.shield.auth.service.util.security;


import db.shield.auth.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class BootstrapAuthorizationService {

    private final UserRepository userRepository;

    public boolean canCreateUser(Authentication authentication) {
        if (userRepository.count() == 0) {
            return true;
        }

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }

        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_READ_WRITE"));
    }
}
