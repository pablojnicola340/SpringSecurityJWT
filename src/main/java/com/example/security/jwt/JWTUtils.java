package com.example.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Component
@Slf4j
public class JWTUtils {

    //----ATRIBUTOS PARA GENERAR EL TOKEN----!!!!!

    //secretKey nos va a ayudar a "firmar" nuestro metodo.
    //Vamos a generar un token que tenemos que firmar, es decir, un sello que indique que nosotros fuimos los que generamos ese token.
    //El token tiene que tener la firma, sino es un token invalido.
    //Podemos desde el navegador buscar "Encryption Key Generator", seleccionamos 256-bit y hexagecimal, la copiamos
    //y la ponemos en application.properties
    @Value("${jwt.secret.key}")
    private String secretKey;

    //Tiempo de expiracion del token.
    //Podemos utilizar un conversor online de dias a milisegundos y añadimos al application.properties
    @Value("${jwt.time.expiration}")
    private String timeExpiration;

    //Metodo para generar un token de acesso
    public String generateAccessToken(String userName) {
        return Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(timeExpiration)))
                //En este paso enviamos la firma encriptada y la vamos a encriptar aun mas
                .signWith(getSignatureKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    //Una vez generado el token necesitamos validar si es correcto
    public boolean isValidToken(String token){
        try {
            //El parserBuilder va a leer el token
            Jwts.parserBuilder()
                    .setSigningKey(getSignatureKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return true;

        }catch(Exception e){
            log.error("Token invalido, error: ".concat(e.getMessage()));
            return false;
        }
    }

    //un JWT se divide en header, payload y signature.
    //En el payload hay ciertos atributos, podemos enviar cualquier valor que queramos encriptar,
    //pero no se puede poner informacion sensible porque se puede desencriptar facilmente.
    //Cada valor del payload se conoce como claim.
    //Debemos obtener el usuario que viene dentro del token, es decir, ese claim.
    //VAMOS A OBTENER TODOS LOS CLAIMS DEL TOKEN
    public Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignatureKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //Obtener username del token
    public String getUserNameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    //Obtener un solo claim
    public <T> T getClaim(String token, Function<Claims, T> claimsTFunction){
        Claims claims = extractAllClaims(token);
        return claimsTFunction.apply(claims);
    }

    //Obtener firma del token
    public Key getSignatureKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        //retornamos la firma encriptada
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
