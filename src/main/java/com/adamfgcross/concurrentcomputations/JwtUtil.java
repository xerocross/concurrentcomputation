package com.adamfgcross.concurrentcomputations;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtil {

	@Value("${spring.secret-key}")
    private String SECRET_KEY;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
    	JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build();
    	try {
    		Jws<Claims> jws = parser.parseClaimsJws(token);
    		return jws.getBody();
    	}  catch (ExpiredJwtException e) {
            throw new AuthenticationFailureException("JWT token is expired", e);
        } catch (UnsupportedJwtException e) {
            throw new AuthenticationFailureException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            throw new AuthenticationFailureException("JWT token is malformed", e);
        } catch (SignatureException e) {
            throw new AuthenticationFailureException("JWT signature validation failed", e);
        } catch (IllegalArgumentException e) {
            throw new AuthenticationFailureException("JWT token is invalid", e);
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes()); // Convert your secret key to the appropriate format
    }

    public String generateToken(Authentication authentication) {
        // Typically, you would include the user's roles or other details in the token
        return Jwts.builder()
                .setSubject(authentication.getName()) // The subject is typically the username
                .claim("roles", authentication.getAuthorities()) // Include roles or other claims
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day expiration
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}
