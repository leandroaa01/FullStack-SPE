package dimap.ufrn.spe.api.v1.dtos;

import dimap.ufrn.spe.api.v1.models.Roles;
import dimap.ufrn.spe.api.v1.models.Situacao;

public record UpdateDTO(String name, String username, String matricula, String email, Roles role,Situacao situacao, String cargo) {
    
}
