package com.asgarov.jwtdemoapp.controller;

import com.asgarov.jwtdemoapp.domain.dto.AuthenticationRequestDto;
import com.asgarov.jwtdemoapp.domain.security.AppUserDetails;
import com.asgarov.jwtdemoapp.services.JwtTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.asgarov.jwtdemoapp.domain.Constants.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    final Set<String> issuedRefreshTokens = Collections.synchronizedSet(new HashSet<>());

    @Value("${jwt.access-token.expires:3600000}")
    private long accessTokenValidityMs;

    @Value("${jwt.refresh-token.expires:86400000}")
    private long refreshTokenValidityMs;


    @PostMapping("login")
    public ResponseEntity<Void> login(@RequestBody AuthenticationRequestDto requestDto,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        try {
            String username = requestDto.username();
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, requestDto.password())
            );
            AppUserDetails authenticatedUser = (AppUserDetails) authentication.getPrincipal();

            createAndAddCookies(
                    request,
                    response,
                    authenticatedUser.getUsername(),
                    authenticatedUser.getRoleNames()
            );

            return ResponseEntity.noContent().build();
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid Credentials");
        }
    }

    @GetMapping("refresh")
    public ResponseEntity<Void> refresh(@CookieValue(value = APP_REFRESH_TOKEN) String refreshToken,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        if (issuedRefreshTokens.contains(refreshToken) && jwtTokenService.validateRefreshToken(refreshToken)) {
            issuedRefreshTokens.remove(refreshToken);
            Claims claims = jwtTokenService.getClaims(refreshToken).getPayload();
            String username = claims.getSubject();
            List<String> rolesNames = (List<String>) claims.get(ROLES);

            createAndAddCookies(request, response, username, rolesNames);
            return ResponseEntity.noContent().build();
        }
        throw new BadCredentialsException("Invalid refresh token");
    }

    private void createAndAddCookies(HttpServletRequest request,
                                     HttpServletResponse response,
                                     String username,
                                     List<String> roleNames) {
        String accessToken = jwtTokenService.createToken(ACCESS_TOKEN_TYPE, username, roleNames, accessTokenValidityMs);
        String refreshToken = jwtTokenService.createToken(REFRESH_TOKEN_TYPE, username, roleNames, refreshTokenValidityMs);

        int secondsSpentOnProcessingRequest = 1;
        int accessTokenExpiresInSeconds = (int) (accessTokenValidityMs / 1000) - secondsSpentOnProcessingRequest;
        int refreshTokenExpiresInSeconds = (int) (refreshTokenValidityMs / 1000) - secondsSpentOnProcessingRequest;

        issuedRefreshTokens.add(refreshToken);

        // secure flag allows cookies to be transported only via https -> we turn it off on localhost
        boolean secureFlag = !LOCALHOST.equalsIgnoreCase(request.getServerName());

        response.addCookie(createCookie(APP_ACCESS_TOKEN, accessToken, secureFlag, accessTokenExpiresInSeconds));
        response.addCookie(createCookie(APP_REFRESH_TOKEN, refreshToken, secureFlag, refreshTokenExpiresInSeconds));
    }

    public static Cookie createCookie(String cookieName, String cookieValue, boolean secure, int expiresIn) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setPath(DEFAULT_PATH);
        cookie.setHttpOnly(HTTP_ONLY);
        cookie.setSecure(secure);
        cookie.setMaxAge(expiresIn);
        return cookie;
    }
}
