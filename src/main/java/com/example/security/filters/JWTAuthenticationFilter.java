package com.example.security.filters;

import com.example.models.UserEntity;
import com.example.security.jwt.JWTUtils;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//Esta clase tengo que configurarla en la clase de configuracion de Spring Security.
//No la anotamos como un bean porque vamos a enviarle varios argumentos y setearle varios atributos.
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private JWTUtils jwtUtils;

    public JWTAuthenticationFilter(JWTUtils jwtUtils){
        this.jwtUtils = jwtUtils;
    }

    //Dos metodos principales

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //Que hacemos cuando el usuario se intente autenticar en nuestra aplicacion?
        //Necesitamos recuperar el usuario que se esta intentando autenticar.
        UserEntity userEntity = null;
        String userName = "";
        String passowrd = "";

        try {
            //Tenemos que mapear el json a objeto java, lo hacemos mediante la libreria Jackson que viene con Spring.
            //Tomamos los parametros userName y password y los vamos a mapear a un userEntity.
            userEntity = new ObjectMapper().readValue(request.getInputStream(), UserEntity.class);
            userName = userEntity.getUsername();
            passowrd = userEntity.getPassword();

        } catch (StreamReadException e) {
            throw new RuntimeException(e);
        } catch (DatabindException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Con este token es con el cual nos vamos a autenticar en la aplicacion.
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userName, passowrd);

        //El authentication manager es el objeto que se encarga de administrar la autenticacion
        return getAuthenticationManager().authenticate(authenticationToken);

        //HASTA ACA SI SALIO BIEN LA COSA NOS ESTAMOS **AUTENTICANDO** EN NUESTRA APLICACION------<
        //ENTONCES SI EL RESULTADO ES EXITOSO VA A PASAR A EJECUTARTE EL METODO "successfulAuthentication()"
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        //Vamos a generar el token:
        //Primero tenemos que obtener los detalles del usuario
        User user = (User) authResult.getPrincipal();

        //Generar el token de acceso
        String token = jwtUtils.generateAccessToken(user.getUsername());

        response.addHeader("Authorization", token);

        //Vamos a mapear la respuesta y la vamos a convertir en un JSON
        Map<String, Object> httpResponse = new HashMap<>();
        httpResponse.put("token", token);
        httpResponse.put("Message", "Autenticacion Correcta");
        httpResponse.put("Username", user.getUsername());

        //Este mapa lo debemos convertir en un JSON mediante la libreria de Jackson
        response.getWriter().write(new ObjectMapper().writeValueAsString(httpResponse));
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().flush(); // Con esto nos estamos asegurando de que todo se escriba correctamente.

        super.successfulAuthentication(request, response, chain, authResult);
    }


}
