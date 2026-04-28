package dimap.ufrn.spe.api.v1.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dimap.ufrn.spe.api.v1.models.Horario;
import dimap.ufrn.spe.api.v1.models.User;

public interface HorarioRepository extends JpaRepository<Horario, Long> {

    List<Horario> findAllByBolsista(User bolsista);

    List<Horario> findAllByBolsistaOrderByIdAsc(User bolsista);

    List<Horario> findAllByBolsistaAndDia(User bolsista, String dia);
}