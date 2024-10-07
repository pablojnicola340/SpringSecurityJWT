package com.example.security;

import com.example.security.filters.JWTAuthenticationFilter;
import com.example.security.filters.JWTAuthorizationFilter;
import com.example.security.jwt.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true) // <------nosotros vamos a habilitar las anotaciones de spring security para nuestros controladores
public class SecurityConfig {

    @Autowired
    JWTUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JWTAuthorizationFilter authorizationFilter;

    //Aca configuramos el comportamiento de acceso de nuestros endpoints, el manejo de la sesion
    //y tenemos una autenticacion basica con un usuario en memoria ---> metodo "userDetailsService()"
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, AuthenticationManager authenticationManager) throws Exception {

        //Instancio el authentication filter
        JWTAuthenticationFilter jwtAuthenticationFilter = new JWTAuthenticationFilter(jwtUtils);
        jwtAuthenticationFilter.setAuthenticationManager(authenticationManager);
        //Por defecto se maneja la ruta /login cuando nos queremos loguear, pero si queremos la podemos cambiar:
        //jwtAuthenticationFilter.setFilterProcessesUrl("/login");

        return httpSecurity
                .csrf(config -> config.disable())
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/hello").permitAll();
                    auth.anyRequest().authenticated();
                })
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .addFilter(jwtAuthenticationFilter)
                .addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /*@Bean
    UserDetailsService userDetailsService() {

        //Lo siguiente nos sirve para crear un usuario en memoria.
        //Para que este usuario pueda funcionar tiene que ser administrado por un objeto que administre
        //la autenticacion, por lo cual tenemos el authenticationManager(), que a su vez nos exige un passwordEncoder.
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("Pablo")
                .password("1234")
                .roles()
                .build());

        return manager;
    }*/ //------> COMENTAMOS ESTO PARA AUTENTICARNOS CON LOS USUARIOS QUE SE ENCONTRARIAN EN LA BASE DE DATOS

    @Bean
    PasswordEncoder passwordEncoder(){
        //Retornamos esto para que no se maneje todavia la encriptacion
        //return NoOpPasswordEncoder.getInstance();

        //Algoritmo de encriptacion
        return new BCryptPasswordEncoder();
    }

    //Vamos a necesitar un authentication manager que se encarga de la autenticacion del usuario.
    //Esto va a requerir un password encoder (definido arriba)
    @Bean
    AuthenticationManager authenticationManager(HttpSecurity httpSecurity, PasswordEncoder passwordEncoder) throws Exception {

        return httpSecurity.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and().build();
    }
}
