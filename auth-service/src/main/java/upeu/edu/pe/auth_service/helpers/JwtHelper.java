package upeu.edu.pe.auth_service.helpers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtHelper {
    private final Logger log = LoggerFactory.getLogger(JwtHelper.class);

    @Value("${application.jwt.secret}")
    private String jwtSecret;

    public String createToken(String username, String role, String email, String whatsapp) {
        final var now = new Date();
        final long threeWeeksInMs = 21L * 24 * 60 * 60 * 1000;
        final var expirationDate = new Date(now.getTime() + threeWeeksInMs);
        return Jwts
                .builder()
                .setSubject(username)
                .claim("role", role)
                .claim("email", email)
                .claim("whatsapp", whatsapp)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .signWith(this.getSecretKey())
                .compact();
    }

    public String extractRole(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(this.getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    //validacion del token
    public boolean validateToken(String token) {
        log.info("JwtHelper:"+token);
        try {
            final var expirationDate = this.getExpirationDate(token);
            return expirationDate.after(new Date());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jwt invalid");
        }

    }
    //verifica si la fecha ya expiro
    private Date getExpirationDate(String token) {
        return this.getClaimsFromToken(token, Claims::getExpiration);
    }
    private <T> T getClaimsFromToken(String token, Function<Claims, T> resolver) {
        return resolver.apply(this.signToken(token));
    }

    //firmar el token
    private Claims signToken(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(this.getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    //crea el key secreto para firmar el token
    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(this.jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

}
