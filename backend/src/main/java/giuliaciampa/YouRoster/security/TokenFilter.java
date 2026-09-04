package giuliaciampa.YouRoster.security;

import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.exceptions.UnauthorizedException;
import giuliaciampa.YouRoster.services.AccountService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TokenFilter extends OncePerRequestFilter {
    private final JWTTools jwtTools;
    private final AccountService accountService;

    public TokenFilter(JWTTools jwtTools, AccountService accountService) {
        this.jwtTools = jwtTools;
        this.accountService = accountService;

    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedException(
                        "Inserire il token nell'authorization nel formato Bearer "
                );
            }

            String accessToken = authHeader.replace("Bearer ", "");

            this.jwtTools.verifyToken(accessToken);

            UUID userId = this.jwtTools.extractIdFromToken(accessToken);
            Account authenticatedAccount = this.accountService.findById(userId);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            authenticatedAccount,
                            null,
                            authenticatedAccount.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (UnauthorizedException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\":\"" + ex.getMessage() + "\"}"
            );
        }
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        AntPathMatcher matcher = new AntPathMatcher();

        return matcher.match("/auth/login", path)
                || matcher.match("/auth/registration", path)
                || matcher.match("/auth/refresh", path)
                || matcher.match("/auth/forgot-password", path)
                || matcher.match("/auth/reset-password", path);
    }

}
