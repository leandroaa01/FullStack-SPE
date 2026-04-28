package dimap.ufrn.spe.api.v1.dtos;

import java.time.LocalDateTime;

import dimap.ufrn.spe.api.v1.models.Situacao;

public record BolsistasDto(Long id, String nome, String username, String matricula, String email, Situacao situacao, LocalDateTime dataCriacao, String cargo) {

}
