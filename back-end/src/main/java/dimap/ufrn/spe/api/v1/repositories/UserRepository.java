package dimap.ufrn.spe.api.v1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import dimap.ufrn.spe.api.v1.models.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
    User findByEmail(String email);
    User findByMatricula(String matricula);
}
