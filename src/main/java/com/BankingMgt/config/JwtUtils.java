package com.BankingMgt.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // Secret key generated for HS256 encryption algorithm
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Token expiration time (24 hours = 86400000 milliseconds)
    private final int jwtExpirationMs = 86400000;

    // 1. Generate a new JWT token using the username
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    // 2. Extract username from the JWT token
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token) // Fixed from parseClaimsJars to parseClaimsJws
                .getBody()             // Gets the claims body
                .getSubject();
    }

    // 3. Validate if the token is authentic and not expired
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(authToken); // Fixed to parseClaimsJws
            return true;
        } catch (Exception e) {
            System.out.println("Invalid JWT Token: " + e.getMessage());
        }
        return false;
    }
}