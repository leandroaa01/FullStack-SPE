package dimap.ufrn.spe.api.v1.dtos;

import java.util.List;

public record PerfilDTO(
    MyData dadosBolsista,
    HorarioSemanaDTO horarioBolsista,
    List<PontoDTO> pontosBolsista, 
    List<JustificativaResponseDTO> justificativaBolsistra
) {}
