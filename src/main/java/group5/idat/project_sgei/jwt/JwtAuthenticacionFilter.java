package group5.idat.project_sgei.jwt;

import java.io.IOException;

import org.hibernate.grammars.hql.HqlParser.SecondContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticacionFilter extends OncePerRequestFilter {

    @Autowired
    private final HandlerExceptionResolver handler;

    @Autowired
    private final JwtService JWT_SERVICE;

    @Autowired
    private final UserDetailsService USER_DETALIS_SERV;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // buscamos el token en la cabecera
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            final String JWT = authHeader.substring(7);
            final String USERNAME = JWT_SERVICE.getUsernameFromToken(JWT);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (USERNAME != null && auth != null) {
                UserDetails userDetails = this.USER_DETALIS_SERV.loadUserByUsername(USERNAME);

                if (JWT_SERVICE.isTokenValid(USERNAME, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()

                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handler.resolveException(request, response, null, e);
        }
    }

}
