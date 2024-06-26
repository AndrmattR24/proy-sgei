package group5.idat.project_sgei.jwt;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    private String getUsernameFromToken(String token) {
        return extractClaimFromClaims(token, Claims::getSubject);
    }

    private Date getExpiredFromToken(String token) {
        return extractClaimFromClaims(token, Claims::getExpiration);
    }

    private String generateToken(Map<String, Object> extraClaims,
            UserDetails userDt, Long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDt.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDt) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDt.getUsername())) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return getExpiredFromToken(token).before(new Date());
    }

    private <T> T extractClaimFromClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
