package com.example.security.filters;

import com.example.security.jwt.JWTUtils;
import com.example.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component //podemos anotarlo como component porque no tenemos que mandarle ningun parametro
public class JWTAuthorizationFilter extends OncePerRequestFilter {
    //OncePerRequestFilter: se va a autenticar una vez por endpoint.
    //Nos obliga a siempre enviar el token de acceso para acceder al recurso.

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        //Primero tenemos que extraer el tokenHeader de la peticion
        String tokenHeader = request.getHeader("Authorization");

        if(tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token= tokenHeader.substring(7);

            if(jwtUtils.isValidToken(token)) {
                String username = jwtUtils.getUserNameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(username, null, userDetails.getAuthorities());

                //SecurityContextHolder contiene la autenticacion propia en la aplicacion
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);

    }
}
