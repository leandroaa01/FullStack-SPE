package dimap.ufrn.spe.api.v1.dtos;

import java.time.LocalDateTime;

public record JustificativaDTO(LocalDateTime data, double qtdDeHoras, String motivo,String justificativa ) {
}