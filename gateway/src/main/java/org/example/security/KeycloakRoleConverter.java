package org.example.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class KeycloakRoleConverter implements Converter<Jwt, Flux<GrantedAuthority>> {

    @Override
    public Flux<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

        if (realmAccess == null || realmAccess.isEmpty()) {
            log.debug("JWT не содержит секцию realm_access или она пуста");
            return Flux.empty();
        }

        if (!(realmAccess.get("roles") instanceof List)) {
            log.debug("Поле roles в JWT не является списком");
            return Flux.empty();
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");

        if (roles == null || roles.isEmpty()) {
            log.debug("JWT не содержит ролей");
            return Flux.empty();
        }

        return Flux.fromIterable(roles)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role));
    }
}