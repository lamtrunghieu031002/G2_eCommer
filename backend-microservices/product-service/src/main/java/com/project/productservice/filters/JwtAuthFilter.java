package com.project.productservice.filters;

import com.project.productservice.components.JwtTokenUtils;
import com.project.productservice.components.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter stateless: neu co Bearer token hop le thi set Authentication tu claims,
 * nguoc lai cho request di tiep nhu anonymous (viec chan do authorizeHttpRequests quyet dinh).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                final String token = authHeader.substring(7);
                if (!jwtTokenUtils.isTokenExpired(token)) {
                    Long userId = jwtTokenUtils.extractUserId(token);
                    String phoneNumber = jwtTokenUtils.extractPhoneNumber(token);
                    String role = jwtTokenUtils.extractRole(token);
                    UserPrincipal principal = new UserPrincipal(userId, phoneNumber, role);
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ignored) {
                // token khong hop le -> tiep tuc nhu anonymous
            }
        }
        filterChain.doFilter(request, response);
    }
}
