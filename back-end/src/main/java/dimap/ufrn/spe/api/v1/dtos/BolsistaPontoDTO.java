package dimap.ufrn.spe.api.v1.dtos;

import java.time.LocalDateTime;

import dimap.ufrn.spe.api.v1.models.Status;

public record BolsistaPontoDTO(String nome, LocalDateTime horaDeEntrada, LocalDateTime horaDeSaida, Status status,double qtdDeHorasFeitas) {}
