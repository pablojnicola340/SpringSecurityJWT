package com.example.service;

import com.example.models.UserEntity;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    //Inyectamos el user repository para que vaya y consulte a la base de datos.
    @Autowired
    private UserRepository userRepository;

    //Este metodo lo consulta Spring Security por debajo, en su core, para asegurarse cual va a ser el usuario que
    //se va a consultar.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //Recuperamos el usuario de la base de datos:
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("El usuario "+username+" no existe"));

        //Generamos la coleccion de roles que iria por parametro en el User que devolvemos mas abajo.
        //NOTA: en el constructor de SimpleGrantedAuthority ponemos "ROLE_" porque Spring Security lo lee asi.
        Collection<? extends GrantedAuthority> authorities = userEntity.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_".concat(role.getName().name())))
                .collect(Collectors.toSet());

        //Retornamos un User de Spring Security
        //Con esto le decimos a Spring Security que el usuario lo tiene que buscar en la base de datos.
        return new User(userEntity.getUsername(),
                userEntity.getPassword(),
                true,
                true,
                true,
                true,
                authorities);
    }
}
