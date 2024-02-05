package com.asgarov.jwtdemoapp.filter;

import com.asgarov.jwtdemoapp.services.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static com.asgarov.jwtdemoapp.domain.Constants.APP_ACCESS_TOKEN;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    @Value("${urls.white-list:/auth/login}")
    private List<String> whiteListedUrls;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (whiteListedUrls.stream().noneMatch(request.getRequestURI()::endsWith)) {
            jwtTokenService.getTokenFromCookie(request, APP_ACCESS_TOKEN)
                    .filter(jwtTokenService::validateToken)
                    .map(jwtTokenService::getAuthentication)
                    .ifPresent(SecurityContextHolder.getContext()::setAuthentication);
        }

        filterChain.doFilter(request, response);
    }
}
