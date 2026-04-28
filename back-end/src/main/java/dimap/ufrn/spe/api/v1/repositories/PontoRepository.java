package dimap.ufrn.spe.api.v1.repositories;

import java.time.LocalDateTime;
import java.util.List;

 import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dimap.ufrn.spe.api.v1.models.Ponto;
import dimap.ufrn.spe.api.v1.models.Status;
import dimap.ufrn.spe.api.v1.models.User;

public interface PontoRepository extends JpaRepository<Ponto, Long> {
    List<Ponto> findAllByBolsista(User bolsista);

    List<Ponto> findAllByBolsistaAndPontoStatus(User bolsista, Status aberto);

    Ponto findByBolsistaAndPontoStatus(User bolsista, Status aberto);
}
