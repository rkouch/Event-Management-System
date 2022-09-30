package tickr.util;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

public class CryptoHelper {
    private static final Key JWT_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256); // TODO

    public static JwtBuilder makeJWTBuilder () {
        return Jwts.builder()
                .signWith(JWT_KEY);
    }

    public static JwtParserBuilder makeJWTParserBuilder () {
        return Jwts.parserBuilder()
                .setSigningKey(JWT_KEY);
    }
}
