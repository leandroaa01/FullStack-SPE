package dimap.ufrn.spe.api.v1.dtos;

import dimap.ufrn.spe.api.v1.models.Roles;

public record LoginResponseDTO(String token, Roles role) {
    
}
