package com.ecommerce.lab.filter;

import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Component
public class JwtUtils {
    
    @Value("${spring.security.jwt-key}")
    private String SECRET_KEY;

    public String generateToken(String email, boolean rememberMe) {
        long expiration = rememberMe ? (1000L * 60 * 60 * 24 * 14) : (1000L * 60 * 60 * 24); // 30 days vs 1 day

        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
            .compact();
    }

    public Boolean validateToken(
        String token,
        UserDetails userDetails,
        HttpServletRequest request
    ) {
        final String username = extractUsername(token, request);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token, request));
    }

    public String extractUsername(String token, HttpServletRequest request) {
        return extractClaim(token, Claims::getSubject, request);
    }

    private Boolean isTokenExpired(String token, HttpServletRequest request) {
        return extractExpiration(token, request).before(new Date());
    }

    private Date extractExpiration(String token, HttpServletRequest request) {
        return extractClaim(token, Claims::getExpiration, request);
    }

    public String extractEmail(String token, HttpServletRequest request) {
        return extractClaim(token, claims -> claims.get("email", String.class), request);
    }

    public <T> T extractClaim(
        String token,
        Function<Claims, T> claimsResolver,
        HttpServletRequest request
    ) {
        try {
            final Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY) // Ensure this is the EXACT same string/byte array
                .parseClaimsJws(token)
                .getBody();
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            // Delete session and spring context holder
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // User user = userre.findByEmail(auth.getName());
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            // If it's Expired, Malformed, or SignatureException
            System.err.println("JWT Extraction Error: " + e.getMessage());
            return null;
        }
    }
}
