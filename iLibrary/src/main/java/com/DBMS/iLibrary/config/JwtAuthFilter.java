package com.DBMS.iLibrary.config;

import com.DBMS.iLibrary.service.UserDetailsServiceImpl;
import com.DBMS.iLibrary.utilities.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService; // optional fallback

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        if (authHeader != null) {
            if (authHeader.startsWith("Bearer ")) {
                // Normal case: "Bearer <token>"
                jwtToken = authHeader.substring(7);
            } else {
                // Fallback: if header accidentally contains JSON
                try {
                    JsonNode node = objectMapper.readTree(authHeader);
                    if (node.has("token")) {
                        jwtToken = node.get("token").asText();
                    }
                } catch (Exception ignored) {
                    // ignore parsing errors — invalid header
                }
            }

            try {
                if (jwtToken != null && !jwtToken.isBlank()) {
                    username = jwtUtil.extractUsername(jwtToken);
                }
            } catch (Exception ex) {
                logger.debug("Invalid JWT token: " + ex.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtUtil.isTokenValid(jwtToken)) {
                    List<SimpleGrantedAuthority> authorities = jwtUtil.extractRoles(jwtToken).stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    logger.debug("JWT token is invalid or expired for user: " + username);
                }
            } catch (Exception ex) {
                logger.debug("Failed to validate JWT token", ex);
            }
        }

        filterChain.doFilter(request, response);
    }
}
