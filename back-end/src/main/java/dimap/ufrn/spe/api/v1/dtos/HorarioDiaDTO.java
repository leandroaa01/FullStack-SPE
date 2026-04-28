package dimap.ufrn.spe.api.v1.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record HorarioDiaDTO(
        @NotBlank String dia,
        @NotEmpty List<@NotBlank String> horariosSelecionados,
        @NotNull Integer totalHoras) {
}