package dimap.ufrn.spe.api.v1.models;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User  implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
     @Column(nullable = false, unique = true)
    private String matricula;

    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;

    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime dataCriacao;

    private Roles roles;
    private Situacao situacao;
    private String cargo;

    @OneToMany(mappedBy = "bolsista", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ponto> pontos; 
    

  @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.roles == Roles.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_BOLSISTA"));
        } 
        return List.of( new SimpleGrantedAuthority("ROLE_BOLSISTA"));
    }
    
    
    public User(String name, String username, String matricula, String password, String email, Roles roles, String cargo) {
    this.username = username;
    this.matricula = matricula;
    this.password = password;
    this.name = name;
    this.email = email;
    this.roles = roles;
    this.cargo = cargo;
    this.situacao = Situacao.ATIVO;
}


    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
