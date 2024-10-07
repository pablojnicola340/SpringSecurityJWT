package com.example.repository;

import com.example.models.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {

    //Tenemos que buscar un usuario por nombre
    //Al utilizar en la firma del metodo el nombre de campo tal cual esta definido, JPA lo va a poder ubicar.
    Optional<UserEntity> findByUsername(String userName);

    //Si queremos utilizar un nombre de campo personalizado podemos hacer lo siguiente;
    @Query("select u from UserEntity u where u.username = ?1") //Con esto especificamos la query que se tiene que ejecutar
    //= ?1 -----> significa que eso va a ser igual al primer parametro que nos encontremos en los argumentos del metodo,
    //en este caso "String userName"
    Optional<UserEntity> getName(String userName);
}
