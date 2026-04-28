package dimap.ufrn.spe.api.v1.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record HorarioSemanaDTO(
        @NotEmpty List<@Valid HorarioDiaDTO> dias) {
}