package org.hackathon.genon.global.security.jwt;


import static org.hackathon.genon.global.security.jwt.enums.TokenType.ACCESS;
import static org.hackathon.genon.global.security.jwt.enums.TokenType.AUTHORIZATION_HEADER;
import static org.hackathon.genon.global.security.jwt.enums.TokenType.REFRESH;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.hackathon.genon.domain.member.enums.Role;
import org.hackathon.genon.global.security.jwt.dto.TokenResponse;
import org.hackathon.genon.global.security.jwt.enums.TokenExpiration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private static final String CATEGORY_KEY = "category";

    public JwtProvider(@Value("${spring.jwt.secretKey}") String key) {
        this.secretKey = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256
                        .key()
                        .build()
                        .getAlgorithm()
        );
    }

    public TokenResponse createTokens(Long memberId, Role role) {
        String accessToken = createAccessToken(memberId, role);
        String refreshToken = createRefreshToken();

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaimsFromToken(token);

        String memberId = claims.getSubject();
        String role = getAuthority(claims).name();
        GrantedAuthority authority = new SimpleGrantedAuthority(role);

        return new UsernamePasswordAuthenticationToken(
                memberId,
                null,
                Collections.singletonList(authority)
        );
    }

    public boolean isValidateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다. Token: {}", token);
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT malformed, 잘못된 형식의 JWT 입니다. Token: {}", token);
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT, 만료된 JWT 입니다. Token: {}", token);
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT, 지원되지 않는 JWT 입니다. Token: {}", token);
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims is empty, 잘못된 JWT 입니다. Token: {}", token);
        }
        return false;
    }


    private String createAccessToken(Long memberId, Role role) {
        Date validity = getTokenExpirationTime(TokenExpiration.ACCESS_TOKEN);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(CATEGORY_KEY, ACCESS.getValue())
                .claim(AUTHORIZATION_HEADER.getValue(), role)
                .issuedAt(new Date())
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    private Date getTokenExpirationTime(TokenExpiration expiration) {
        Date date = new Date();
        return new Date(date.getTime() + expiration.getExpirationTime());
    }

    private String createRefreshToken() {
        Date validity = getTokenExpirationTime(TokenExpiration.REFRESH_TOKEN);

        return Jwts.builder()
                .claim(CATEGORY_KEY, REFRESH.getValue())
                .issuedAt(new Date())
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    private Role getAuthority(Claims claims) {
        return Role.valueOf(claims.get(AUTHORIZATION_HEADER.getValue(), String.class));
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void validateExpiredToken(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration.before(new Date())) {
            throw new ExpiredJwtException(null, claims, "만료된 JWT 입니다.");
        }
    }

}