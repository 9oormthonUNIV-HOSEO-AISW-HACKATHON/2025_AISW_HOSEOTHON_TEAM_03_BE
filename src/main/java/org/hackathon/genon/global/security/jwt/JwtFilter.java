package org.hackathon.genon.global.security.jwt;

import static org.hackathon.genon.global.error.ErrorStatus.INVALID_TOKEN;
import static org.hackathon.genon.global.security.jwt.enums.TokenType.AUTHORIZATION_HEADER;
import static org.hackathon.genon.global.security.jwt.enums.TokenType.BEARER_PREFIX;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hackathon.genon.global.error.response.ErrorResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final List<String> EXCLUDE_PATHS = List.of(
            "/", "/error", "/v1/auth/**", "/v1/members/register",
            "/swagger/**", "/swagger-ui/**", "/v3/api-docs/**"
    );
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return EXCLUDE_PATHS.stream()
                .anyMatch(exclude -> PATH_MATCHER.match(exclude, path));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Optional<String> tokenOpt = getTokenFromHeader(request);

        // 토큰이 없는 경우
        if (tokenOpt.isEmpty()) {
            sendErrorResponse(response);
            return;
        }

        // 토큰이 존재하지만 유효하지 않은 경우
        String token = tokenOpt.get();
        if (!jwtProvider.isValidateToken(token)) {
            sendErrorResponse(response);
            return;
        }


        Authentication authentication = jwtProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private Optional<String> getTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER.getValue());

        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX.getValue())) {
            String token = bearerToken.substring(BEARER_PREFIX.getValue().length());

            return Optional.of(token);
        }

        return Optional.empty();
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setStatus(UNAUTHORIZED.value());
        response.setContentType(APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(UNAUTHORIZED.value())
                .message(INVALID_TOKEN.getMessage())
                .build();

        log.warn("JWT 예외: {}", INVALID_TOKEN.getMessage());

        String jsonToString = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonToString);
    }

}