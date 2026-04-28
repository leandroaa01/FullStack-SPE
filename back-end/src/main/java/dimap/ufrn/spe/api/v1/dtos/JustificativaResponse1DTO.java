package dimap.ufrn.spe.api.v1.dtos;

import java.time.LocalDateTime;

import dimap.ufrn.spe.api.v1.models.Status;

public record JustificativaResponse1DTO(LocalDateTime data, double qtdDeHoras, String motivo,String justificativa, Status justificaSituacao, Long id, String nomeBolsista, Long idBolsista) {
}