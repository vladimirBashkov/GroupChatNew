package main.security.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import main.security.SecurityUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.tokenExpiration}")
    private Duration tokenExpiration;

    public String generateJwtToken(SecurityUser securityUser){
        return generateTokenFromLogin(securityUser.getLogin());
    }

    public String generateTokenFromLogin(String login) {
        return Jwts.builder()
                .setSubject(login)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + tokenExpiration.toMillis()))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUsername(String token){
        return Jwts.parser().setSigningKey(jwtSecret)
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validate(String authToken){
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Token is expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Token is unsupported: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("Claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
}
