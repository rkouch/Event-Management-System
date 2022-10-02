package tickr.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
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

    public static char[] hashPassword (String password) {
        return BCrypt.withDefaults().hashToChar(10, password.toCharArray());
    }

    public static boolean verifyHash (String password, char[] hash) {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified;
    }
}
