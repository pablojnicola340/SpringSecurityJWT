package com.example.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data //para generar getters y setters
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email //nos seguramos que se tenga que insertar un mail
    @NotBlank
    @Size(max=80)
    private String email;

    @NotBlank
    @Size(max=30)
    private String username;

    @NotBlank
    private String password;

    //Establecemos la relacion con la tables roles
    //Trabajamos con Set porque no nos permite tener elementos duplicados.
    @ManyToMany(fetch=FetchType.EAGER,
            targetEntity=RoleEntity.class,
            cascade = CascadeType.PERSIST) //Cuando yo consulte ese usuario necesito que me traiga todos los roles asociados a ese usuario de una vez,
                                           //por eso el FetchType es EAGER. Con LAZY por ejemplo me traeria uno por uno cuando yo lo solicite.

                                           //En "targetEntity" indicamos con que entidad se va a establecer la relacion.

                                           //CascadeType.PERSIST: cuando ingrese un usuario a la base de datos necesito que de una vez inserte los roles,
                                           //pero si el usuario se elimina no puedo permitir que borre los roles. Por eso que necesitamos que el cascade
                                           //sea cuando se persiste un usuario.

    //Configuramos las claves foraneas
    //Recordar que cuando hay una relacion many to many vamos a tener una tabla intermedia, la cual
    //se va a llamar en este caso "user_roles"
    //JoinColumns ---> indicamos la clave foranea de usuarios y la clave foranea de roles.
    //@JoinColumn(name="user_id") --->clave foranea de la entidad UserEntity
    //inverseJoinColumns = @JoinColumn(name="role_id")  --->clave foranea de la entidad RoleEntity
    @JoinTable(name="user_roles",
               joinColumns = @JoinColumn(name="user_id"),
               inverseJoinColumns = @JoinColumn(name="role_id"))
    private Set<RoleEntity> roles;
}
