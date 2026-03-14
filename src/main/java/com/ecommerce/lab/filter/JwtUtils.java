package com.ecommerce.lab.filter;

import java.util.Date;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.ecommerce.lab.service.CustomUserDetailsService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtils {
    // temp key
    private static final String SECRET_KEY = "a2V5MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=";
    private CustomUserDetailsService userDetailsService;

    public JwtUtils(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public String generateToken(String email, boolean rememberMe) {
        System.out.println("====>> " + email + " " + rememberMe);
        long expiration = rememberMe ? (1000L * 60 * 60 * 24 * 30) : (60_000); // 30 days vs 1 day

        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 6 mins // 1000 * 60
                                                                              // *
                                                                              // 60 * 24
            .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
            .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // System.out.println(
        //     "======> " + username.equals(userDetails.getUsername()) + username + " "
        //         + userDetails.getUsername()
        // );
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String extractUsername(String token) { return extractClaim(token, Claims::getSubject); }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY) // Ensure this is the EXACT same string/byte array
                .parseClaimsJws(token)
                .getBody();
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            // If it's Expired, Malformed, or SignatureException
            System.err.println("JWT Extraction Error: " + e.getMessage());
            return null;
        }
    }
}
