package com.taller.seguridad.notas_seguras.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    private static final String SECRET_KEY = "supersecretoyseguro123supersecretoyseguro123"; // 32+ caracteres
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hora

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> validateToken(String token) throws JwtException {
        try {
            System.out.println("=== JWT VALIDATION ===");
            System.out.println("Token: " + token);

            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            System.out.println("Token validated successfully");
            System.out.println("Subject: " + claims.getBody().getSubject());
            System.out.println("Role: " + claims.getBody().get("role"));
            System.out.println("Expiration: " + claims.getBody().getExpiration());

            return claims;
        } catch (ExpiredJwtException e) {
            System.err.println("Token expirado: " + e.getMessage());
            throw new JwtException("Token expirado");
        } catch (SignatureException e) {
            System.err.println("Firma del token inválida: " + e.getMessage());
            throw new JwtException("Token inválido");
        } catch (MalformedJwtException e) {
            System.err.println("Token mal formado: " + e.getMessage());
            throw new JwtException("Token mal formado");
        } catch (JwtException e) {
            System.err.println("Error validando token: " + e.getMessage());
            throw e;
        }
    }

    public String getEmailFromToken(String token) {
        return validateToken(token).getBody().getSubject();
    }

    // Método para validar sin lanzar excepción (útil para verificaciones)
    public boolean isValidToken(String token) {
        try {
            validateToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}