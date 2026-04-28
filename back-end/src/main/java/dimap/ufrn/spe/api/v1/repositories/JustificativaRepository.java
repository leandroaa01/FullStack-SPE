package dimap.ufrn.spe.api.v1.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dimap.ufrn.spe.api.v1.models.Justificativa;
import dimap.ufrn.spe.api.v1.models.User;

public interface JustificativaRepository extends JpaRepository<Justificativa, Long> {

    List<Justificativa> findAllByBolsistaOrderByDataDesc(User bolsista);

    // Busca justificativa por bolsista e data (ignorando hora)
    @org.springframework.data.jpa.repository.Query("SELECT j FROM Justificativa j WHERE j.bolsista = :bolsista AND DATE(j.data) = :data")
    Optional<Justificativa> findByBolsistaAndData(dimap.ufrn.spe.api.v1.models.User bolsista, java.time.LocalDate data);

    Optional<Justificativa> findByIdAndBolsista(Long id, User bolsista);
}
