package com.taller.seguridad.notas_seguras.security;

import com.taller.seguridad.notas_seguras.model.User;
import com.taller.seguridad.notas_seguras.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository; // Agrega esto

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository; // Inyecta UserRepository
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        System.out.println("=== JWT FILTER ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Authorization header: " + header);

        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("No Bearer token found, continuing chain");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        System.out.println("Token found, length: " + token.length());

        try {
            Jws<Claims> claims = jwtService.validateToken(token);
            String email = claims.getBody().getSubject();
            System.out.println("Token validated for user: " + email);

            // Verificar que el usuario existe en la base de datos
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                System.out.println("User not found in database: " + email);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Usuario no encontrado");
                return;
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            email, // Usar el email como principal
                            null,
                            Collections.emptyList()
                    );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println("Authentication set in SecurityContext");

        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token inválido o expirado");
            return;
        }

        filterChain.doFilter(request, response);
    }
}