package com.asgarov.jwtdemoapp.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.asgarov.jwtdemoapp.domain.Constants.REFRESH_TOKEN_TYPE;
import static com.asgarov.jwtdemoapp.domain.Constants.ROLES;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    public static final String JWT_TOKEN_TYPE = "type";

    private final UserDetailsService userDetailsService;
    final String secret = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());

    public String createToken(String type, String username, List<String> roleNames, long validityInMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validityInMillis);

        return Jwts.builder()
                .claims(Map.of(ROLES, roleNames))
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .header()
                .add(JWT_TOKEN_TYPE, type)
                .and()
                .compact();
    }

    public Optional<String> getTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = Objects.requireNonNullElse(request.getCookies(), new Cookie[]{});
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public boolean validateToken(String token) {
        Date expirationDate = getClaims(token).getPayload().getExpiration();
        return !expirationDate.before(new Date());
    }


    public boolean validateRefreshToken(String refreshToken) {
        if (!REFRESH_TOKEN_TYPE.equals(getClaims(refreshToken).getHeader().get(JWT_TOKEN_TYPE))) {
            throw new IllegalArgumentException("The token provided is not a %s".formatted(REFRESH_TOKEN_TYPE));
        }
        return validateToken(refreshToken);
    }

    public Authentication getAuthentication(String token) {
        String username = getClaims(token).getPayload().getSubject();
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public Jws<Claims> getClaims(String token) {
        return Jwts.parser().
                verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build().parseSignedClaims(token);
    }
}

